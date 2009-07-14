/*
	Copyright (c) 2009 Olivier Chafik, All Rights Reserved
	
	This file is part of JNAerator (http://jnaerator.googlecode.com/).
	
	JNAerator is free software: you can redistribute it and/or modify
	it under the terms of the GNU Lesser General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	JNAerator is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU Lesser General Public License for more details.
	
	You should have received a copy of the GNU Lesser General Public License
	along with JNAerator.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.ochafik.lang.jnaerator;

import static com.ochafik.lang.SyntaxUtils.as;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

import org.rococoa.cocoa.foundation.NSObject;

import com.ochafik.lang.jnaerator.JNAeratorConfig.GenFeatures;
import com.ochafik.lang.jnaerator.TypeConversion.JavaPrim;
import com.ochafik.lang.jnaerator.TypeConversion.TypeConversionMode;
import com.ochafik.lang.jnaerator.cplusplus.CPlusPlusMangler;
import com.ochafik.lang.jnaerator.parser.*;
import com.ochafik.lang.jnaerator.parser.Enum;
import com.ochafik.lang.jnaerator.parser.Function;
import com.ochafik.lang.jnaerator.parser.Scanner;
import com.ochafik.lang.jnaerator.parser.Statement.Block;
import com.ochafik.lang.jnaerator.parser.StoredDeclarations.*;
import com.ochafik.lang.jnaerator.parser.TypeRef.*;
import com.ochafik.lang.jnaerator.parser.Expression.*;
import com.ochafik.lang.jnaerator.parser.Function.Type;
import com.ochafik.lang.jnaerator.parser.Declarator.*;
import com.ochafik.util.listenable.Pair;
import com.ochafik.util.string.StringUtils;
import com.sun.jna.*;
import com.sun.jna.Pointer;

import static com.ochafik.lang.jnaerator.parser.ElementsHelper.*;
public class DeclarationsConverter {
	private static final int MAX_FIELDS_FOR_VALUES_CONSTRUCTORS = 10;

	public DeclarationsConverter(Result result) {
		this.result = result;
	}

	final Result result;
	
	
	void convertCallback(FunctionSignature functionSignature, Signatures signatures, DeclarationsHolder out, Identifier callerLibraryName) {
		Identifier name = result.typeConverter.inferCallBackName(functionSignature, false, false);
		if (name == null)
			return;
		
		name = result.typeConverter.getValidJavaArgumentName(name);
		
		Function function = functionSignature.getFunction();
		
		int i = 1;
		Identifier chosenName = name;
		while (!(signatures.classSignatures.add(chosenName))) {
			chosenName = ident(name.toString() + (++i));
		}
		
		Element parent = functionSignature.getParentElement();
		Element comel = parent != null && parent instanceof TypeDef ? parent : functionSignature;
		
		Struct callbackStruct = new Struct();
		callbackStruct.setType(Struct.Type.JavaInterface);
		callbackStruct.addModifiers(Modifier.Public);
		callbackStruct.setParents(Arrays.asList(ident(Callback.class)));
		callbackStruct.setTag(ident(chosenName));
		callbackStruct.addToCommentBefore(comel.getCommentBefore(), comel.getCommentAfter(), getFileCommentContent(comel));
		convertFunction(function, new Signatures(), true, callbackStruct, callerLibraryName);
		for (Declaration d : callbackStruct.getDeclarations()) {
			if (d instanceof Function) {
				callbackStruct.addAnnotations(callbackStruct.getAnnotations());
				callbackStruct.setAnnotations(null);
				break;
			}
		}
		out.addDeclaration(new TaggedTypeRefDeclaration(callbackStruct));
	}

	void convertCallbacks(List<FunctionSignature> functionSignatures, Signatures signatures, DeclarationsHolder out, Identifier libraryClassName) {
		if (functionSignatures != null) {
			for (FunctionSignature functionSignature : functionSignatures) {
				if (functionSignature.findParentOfType(Struct.class) != null)
					continue;
				
				Arg a = functionSignature.findParentOfType(Arg.class);
				if (a != null && a.getParentElement() == null)
					continue;//TODO understand why we end up having an orphan Arg here !!!!
					
				convertCallback(functionSignature, signatures, out, libraryClassName);
			}
		}
		
	}

	public static class DeclarationsOutput {
		Map<String, DeclarationsHolder> holders = new HashMap<String, DeclarationsHolder>();
		public void add(Declaration d, String libraryName) {
			
		}
		public void set(String libraryName, DeclarationsHolder holder) {
			
		}
	}
	void convertConstants(List<Define> defines, Element sourcesRoot, final Signatures signatures, final DeclarationsHolder out, final Identifier libraryClassName) {
		//final List<Define> defines = new ArrayList<Define>();
		sourcesRoot.accept(new Scanner() {
//			@Override
//			public void visitDefine(Define define) {
//				super.visitDefine(define);
//				if (elementsFilter.accept(define))
//					defines.add(define);
//			}
			@Override
			public void visitVariablesDeclaration(VariablesDeclaration v) {
				super.visitVariablesDeclaration(v);
				//if (!elementsFilter.accept(v))
				//	return;
				
				if (v.findParentOfType(Struct.class) != null)
					return;
				
				if (v.getValueType() instanceof FunctionSignature)
					return;
					
				for (Declarator vs : v.getDeclarators()) {
					if (!(vs instanceof DirectDeclarator))
						continue; // TODO provide a mapping of exported values
					
					TypeRef mutatedType = (TypeRef) vs.mutateType(v.getValueType());
					if (mutatedType == null || 
							!mutatedType.getModifiers().contains(Modifier.Const) ||
							mutatedType.getModifiers().contains(Modifier.Extern) ||
							vs.getDefaultValue() == null)
						continue;
					
					//TypeRef type = v.getValueType();
					JavaPrim prim = result.typeConverter.getPrimitive(mutatedType, libraryClassName);
					if (prim == null)
						continue;
					
					try {
						
						DirectDeclarator dd = (DirectDeclarator)vs;
						Expression val = result.typeConverter.convertExpressionToJava(vs.getDefaultValue(), libraryClassName);
						
						if (!signatures.variablesSignatures.add(vs.resolveName()))
							continue;
						
						TypeRef tr = result.typeConverter.convertTypeToJNA(mutatedType, TypeConversion.TypeConversionMode.FieldType, libraryClassName);
						VariablesDeclaration vd = new VariablesDeclaration(tr, new DirectDeclarator(dd.getName(), val));
						vd.setCommentBefore(v.getCommentBefore());
						vd.addToCommentBefore(vs.getCommentBefore());
						vd.addToCommentBefore(vs.getCommentAfter());
						vd.addToCommentBefore(v.getCommentAfter());
						
						out.addDeclaration(vd);
					} catch (UnsupportedConversionException e) {
						out.addDeclaration(skipDeclaration(v, e.toString()));
					}
					
				}
			}
		});
		
		if (defines != null) {
			for (Define define : reorderDefines(defines)) {
				if (define.getValue() == null)
					continue;
				
				try {
					out.addDeclaration(outputConstant(define.getName(), define.getValue(), signatures, define.getValue(), "define", libraryClassName, true, false, false));
				} catch (UnsupportedConversionException ex) {
					out.addDeclaration(skipDeclaration(define, ex.toString()));
				}
			}
		}
	}

	static Map<Class<?>, Pair<List<Pair<Function, String>>, Set<String>>> cachedForcedMethodsAndTheirSignatures;
	
	public static synchronized Pair<List<Pair<Function,String>>,Set<String>> getMethodsAndTheirSignatures(Class<?> originalLib) {
		if (cachedForcedMethodsAndTheirSignatures == null)
			cachedForcedMethodsAndTheirSignatures = new HashMap<Class<?>, Pair<List<Pair<Function, String>>,Set<String>>>();

		Pair<List<Pair<Function, String>>, Set<String>> pair = cachedForcedMethodsAndTheirSignatures.get(originalLib);
		if (pair == null) {
			pair = new Pair<List<Pair<Function, String>>, Set<String>>(new ArrayList<Pair<Function, String>>(), new HashSet<String>());
			for (Method m : originalLib.getDeclaredMethods()) {
				Function f = Function.fromMethod(m);
				String sig = f.computeSignature(false);
				pair.getFirst().add(new Pair<Function, String>(f, sig));
				pair.getSecond().add(sig);
			}
		}
		return pair;
	}
	
	public void addMissingMethods(Class<?> originalLib, Signatures existingSignatures, Struct outputLib) {
		for (Pair<Function, String> f : getMethodsAndTheirSignatures(originalLib).getFirst())
			if (existingSignatures.methodsSignatures.add(f.getSecond()))
				outputLib.addDeclaration(f.getFirst().clone());
	}
	
	EmptyDeclaration skipDeclaration(Element e, String... preMessages) {
		if (result.config.limitComments)
			return null;
		
		List<String> mess = new ArrayList<String>();
		if (preMessages != null)
			mess.addAll(Arrays.asList(preMessages));
		mess.addAll(Arrays.asList("SKIPPED:", e.formatComments("", true, true, false), getFileCommentContent(e), e.toString().replace("*/", "* /")));
		return new EmptyDeclaration(mess.toArray(new String[0]));
	}
	
	void convertEnum(Enum e, Signatures signatures, DeclarationsHolder out, Identifier libraryClassName) {
		if (e.isForwardDeclaration())
			return;
		
		DeclarationsHolder localOut = out;
		Signatures localSignatures = signatures;
		
		Struct enumInterf = null;
		Identifier enumName = getActualTaggedTypeName(e);
		boolean repeatFullEnumComment;
		if (enumName != null && enumName.resolveLastSimpleIdentifier().getName() != null) {
			if (!signatures.classSignatures.add(enumName))
				return;
			
			repeatFullEnumComment = false;
			
			enumInterf = publicStaticClass(enumName, null, Struct.Type.JavaInterface, e);
			if (result.config.features.contains(JNAeratorConfig.GenFeatures.EnumTypeLocationComments))
				enumInterf.addToCommentBefore("enum values");
			out.addDeclaration(new TaggedTypeRefDeclaration(enumInterf));
			
			localSignatures = new Signatures();
			localOut = enumInterf;
		} else {
			repeatFullEnumComment = true;
		}
		Integer lastAdditiveValue = null;
		Expression lastRefValue = null;
		boolean failedOnceForThisEnum = false;
		for (com.ochafik.lang.jnaerator.parser.Enum.EnumItem item : e.getItems()) {
			Expression resultingExpression;
			try {
				if (item.getValue() == null) {
					// no explicit value
					if (lastRefValue == null) {
						if (lastAdditiveValue != null) {
							lastAdditiveValue++;
							resultingExpression = expr(Constant.Type.Int, lastAdditiveValue);
						} else {
							if (item == e.getItems().get(0)) {
								lastAdditiveValue = 0;
								resultingExpression = expr(Constant.Type.Int, lastAdditiveValue);
							} else
								resultingExpression = null;
						}
					} else {
						// has a last reference value
						if (lastAdditiveValue != null)
							lastAdditiveValue++;
						else
							lastAdditiveValue = 1;
						
						resultingExpression = //result.typeConverter.convertExpressionToJava(
							expr(
								lastRefValue.clone(), 
								Expression.BinaryOperator.Plus, 
								expr(Constant.Type.Int, lastAdditiveValue)
							//)
						);
					}
				} else {
					// has an explicit value
					failedOnceForThisEnum = false;// reset skipping
					lastAdditiveValue = null;
					lastRefValue = item.getValue();
					resultingExpression = lastRefValue;
					if (lastRefValue instanceof Expression.Constant) {
						try {
							lastAdditiveValue = ((Expression.Constant)lastRefValue).asInteger();
							lastRefValue = null;
						} catch (Exception ex) {}
					}	
				}
			} catch (Exception ex) {
				//ex.printStackTrace();
				resultingExpression = null;
			}
			failedOnceForThisEnum = failedOnceForThisEnum || resultingExpression == null;
			if (failedOnceForThisEnum)
				out.addDeclaration(skipDeclaration(item));
			else {
				try {
					Declaration ct = outputConstant(
						item.getName(), 
						result.typeConverter.convertExpressionToJava(resultingExpression, libraryClassName), 
						localSignatures, 
						item, 
						"enum item", 
						libraryClassName, 
						enumInterf == null,
						true,
						true
					);
					if (ct != null && repeatFullEnumComment) {
						String c = ct.getCommentBefore();
						ct.setCommentBefore(e.getCommentBefore());
						ct.addToCommentBefore(c);
					}
					localOut.addDeclaration(ct);
				} catch (Exception ex) {
					out.addDeclaration(skipDeclaration(item, ex.toString()));
				}
			}
		}
		//if (enumInterf != null)
		//	enumInterf.addDeclarations(localOut);
	}

	@SuppressWarnings("static-access")
	private Declaration outputConstant(String name, Expression x, Signatures signatures, Element element, String elementTypeDescription, Identifier libraryClassName, boolean addFileComment, boolean signalErrors, boolean forceInteger) throws UnsupportedConversionException {
		try {
			if (result.typeConverter.isJavaKeyword(name))
				throw new UnsupportedConversionException(element, "The name '" + name + "' is invalid for a Java field.");
			
			Expression converted = result.typeConverter.convertExpressionToJava(x, libraryClassName);
			TypeRef tr = result.typeConverter.inferJavaType(converted);
			JavaPrim prim = result.typeConverter.getPrimitive(tr, libraryClassName);
			
			if (forceInteger && prim == JavaPrim.Boolean) {
				prim = JavaPrim.Int;
				tr = typeRef("int");
				converted = expr(Constant.Type.Int, "true".equals(String.valueOf(converted.toString())) ? 1 : 0);
			}
			
			if ((prim == null || tr == null) && signalErrors) {
				if (result.config.limitComments)
					return null;
				
				return new EmptyDeclaration("Failed to infer type of " + converted);
			} else if (prim != JavaPrim.Void && tr != null) {
//				if (prim == JavaPrim.Int)
//					tr = typeRef("long");
				
				if (signatures.variablesSignatures.add(name)) {
					String t = converted.toString();
					if (t.contains("sizeof")) {
						converted = result.typeConverter.convertExpressionToJava(x, libraryClassName);
					}

					//TypeRef tr = new TypeRef.SimpleTypeRef(result.typeConverter.typeToJNA(type, vs, TypeConversion.TypeConversionMode.FieldType, callerLibraryClass));
					Declaration declaration = new VariablesDeclaration(tr, new DirectDeclarator(name, converted));
					declaration.addModifiers(Modifier.Public, Modifier.Static, Modifier.Final);
					declaration.importDetails(element, false);
					declaration.moveAllCommentsBefore();
					if (addFileComment)
						declaration.addToCommentBefore(getFileCommentContent(element));
					return declaration;
				}
			}
			return skipDeclaration(element, elementTypeDescription);
		} catch (UnsupportedConversionException e) {
			return skipDeclaration(element, elementTypeDescription, e.toString());
		}	
		
	} 

	void convertEnums(List<Enum> enums, Signatures signatures, DeclarationsHolder out, Identifier libraryClassName) {
		if (enums != null) {
			//out.println("public static class ENUMS {");
			for (com.ochafik.lang.jnaerator.parser.Enum e : enums) {
				if (e.findParentOfType(Struct.class) != null)
					continue;
				
				convertEnum(e, signatures, out, libraryClassName);
			}
			//out.println("}");
		}
	}

	void convertFunction(Function function, Signatures signatures, boolean isCallback, DeclarationsHolder out, Identifier libraryClassName) {
		if (result.config.functionsAccepter != null && !result.config.functionsAccepter.adapt(function))
			return;
		
		//if (function.findParentOfType(Template))
		Identifier functionName = function.getName();
		if (functionName == null) {
			if (function.getParentElement() instanceof FunctionSignature)
				functionName = ident("invoke");
			else
				return;
		}
		
		if (functionName.toString().contains("<")) {
			return;
		}
		functionName = result.typeConverter.getValidJavaMethodName(functionName);
		if (functionName == null)
			return;
		
		Function natFunc = new Function();
		
		Element parent = function.getParentElement();
		List<String> ns = new ArrayList<String>(function.getNameSpace());
		boolean isMethod = parent instanceof Struct;
		if (isMethod) {
			ns.clear();
			ns.addAll(parent.getNameSpace());
			switch (((Struct)parent).getType()) {
			case ObjCClass:
			case ObjCProtocol:
				break;
			case CPPClass:
				ns.add(((Struct)parent).getTag().toString());
				break;
			}
		}
		//String namespaceArrayStr = "{\"" + StringUtils.implode(ns, "\", \"") + "\"}";
		//if (!ns.isEmpty())
		//	natFunc.addAnnotation(new Annotation(Namespace.class, "(value=" + namespaceArrayStr + (isMethod ? ", isClass=true" : "") + ")"));
		
		natFunc.setType(Function.Type.JavaMethod);
		if (result.config.useJNADirectCalls && !isCallback) {
			natFunc.addModifiers(Modifier.Public, Modifier.Static, Modifier.Native);
		}
		try {
			//StringBuilder outPrefix = new StringBuilder();
			TypeRef returnType = null;
			
			boolean isObjectiveC = function.getType() == Type.ObjCMethod;
			if (!isObjectiveC) {
				returnType = function.getValueType();
				if (returnType == null)
					returnType = new TypeRef.Primitive("int");
			} else {
				returnType = RococoaUtils.fixReturnType(function);
				functionName = ident(RococoaUtils.getMethodName(function));
			}
			
			Identifier modifiedMethodName;
			if (isCallback) {
				modifiedMethodName = ident("invoke");
			} else {
				modifiedMethodName = result.typeConverter.getValidJavaMethodName(ident(StringUtils.implode(ns, result.config.cPlusPlusNameSpaceSeparator) + (ns.isEmpty() ? "" : result.config.cPlusPlusNameSpaceSeparator) + functionName));
			}
			Set<String> names = new LinkedHashSet<String>();
			//if (ns.isEmpty())
			
			if (function.getType() == Type.CppMethod && !function.getModifiers().contains(Modifier.Static))
				return;
			
			if (!isCallback && result.config.features.contains(JNAeratorConfig.GenFeatures.CPlusPlusMangling))
				addCPlusPlusMangledNames(function, names);
			
			if (!modifiedMethodName.equals(functionName) && ns.isEmpty())
				names.add(function.getName().toString());
			if (function.getAsmName() != null)
				names.add(function.getAsmName());
			
			if (!isCallback && !names.isEmpty())
				natFunc.addAnnotation(new Annotation(Mangling.class, "({\"" + StringUtils.implode(names, "\", \"") + "\"})"));

			//if (isCallback || !modifiedMethodName.equals(functionName))
			//	natFunc.addAnnotation(new Annotation(Name.class, "(value=\"" + functionName + "\"" + (ns.isEmpty() ? "" : ", namespace=" + namespaceArrayStr)  + (isMethod ? ", classMember=true" : "") + ")"));
			
			natFunc.setName(modifiedMethodName);
			natFunc.setValueType(result.typeConverter.convertTypeToJNA(returnType, TypeConversionMode.ReturnType, libraryClassName));
			natFunc.importDetails(function, false);
			natFunc.moveAllCommentsBefore();
			if (!isCallback)
				natFunc.addToCommentBefore(getFileCommentContent(function));
			
			boolean alternativeOutputs = !isCallback;
			
			Function primFunc = alternativeOutputs ? natFunc.clone() : null;
			Function bufFunc = alternativeOutputs ? natFunc.clone() : null;
			
			Set<String> argNames = new TreeSet<String>();
//			for (Arg arg : function.getArgs())
//				if (arg.getName() != null) 
//					argNames.add(arg.getName());
				
			int iArg = 1;
			for (Arg arg : function.getArgs()) {
				if (arg.isVarArg() && arg.getValueType() == null) {
					//TODO choose vaname dynamically !
					Identifier vaType = ident(isObjectiveC ? NSObject.class : Object.class);
					String argName = chooseJavaArgName("varargs", iArg, argNames);
					natFunc.addArg(new Arg(argName, typeRef(vaType.clone()))).setVarArg(true);
					if (alternativeOutputs) {
						primFunc.addArg(new Arg(argName, typeRef(vaType.clone()))).setVarArg(true);
						bufFunc.addArg(new Arg(argName, typeRef(vaType.clone()))).setVarArg(true);
					}
				} else {
					String argName = chooseJavaArgName(arg.getName(), iArg, argNames);
					
					TypeRef mutType = arg.createMutatedType();
					if (mutType == null)
						throw new UnsupportedConversionException(function, "Argument " + arg.getName() + " cannot be converted");
					
					if (mutType.toString().contains("NSOpenGLContextParameter")) {
						argName = argName.toString();
					}
					natFunc.addArg(new Arg(argName, result.typeConverter.convertTypeToJNA(mutType, TypeConversionMode.NativeParameter, libraryClassName)));
					if (alternativeOutputs) {
						primFunc.addArg(new Arg(argName, result.typeConverter.convertTypeToJNA(mutType, TypeConversionMode.PrimitiveParameter, libraryClassName)));
						bufFunc.addArg(new Arg(argName, result.typeConverter.convertTypeToJNA(mutType, TypeConversionMode.BufferParameter, libraryClassName)));
					}
				}
				iArg++;
			}
			
			String natSign = natFunc.computeSignature(false),
				primSign = alternativeOutputs ? primFunc.computeSignature(false) : null,
				bufSign = alternativeOutputs ? bufFunc.computeSignature(false) : null;
				
			if (signatures == null || signatures.methodsSignatures.add(natSign)) {
				if (alternativeOutputs && !primSign.equals(natSign)) {
					if (primSign.equals(bufSign))
						natFunc.addToCommentBefore(Arrays.asList("@deprecated use the safer method {@link #" + primSign + "} instead"));
					else
						natFunc.addToCommentBefore(Arrays.asList("@deprecated use the safer methods {@link #" + primSign + "} and {@link #" + bufSign + "} instead"));
					natFunc.addAnnotation(new Annotation(Deprecated.class));
				}
				collectParamComments(natFunc);
				out.addDeclaration(natFunc);
			}
			
			if (alternativeOutputs) {
				if (signatures == null || signatures.methodsSignatures.add(primSign)) {
					collectParamComments(primFunc);
					out.addDeclaration(primFunc);
				}
				if (signatures == null || signatures.methodsSignatures.add(bufSign)) {
					collectParamComments(bufFunc);
					out.addDeclaration(bufFunc);
				}
			}
		} catch (UnsupportedConversionException ex) {
			if (!result.config.limitComments)
				out.addDeclaration(new EmptyDeclaration(getFileCommentContent(function), ex.toString()));
		}
	}

	protected boolean isCPlusPlusFileName(String file) {
		if (file == null)
			return true;
			
		file = file.toLowerCase();
		return !file.endsWith(".c") && !file.endsWith(".m");
	}
	private void addCPlusPlusMangledNames(Function function, Set<String> names) {
		if (function.getType() == Type.ObjCMethod)
			return;
		
		String elementFile = function.getElementFile();
		if (elementFile != null && elementFile.contains(".framework/"))
			return;
		
		ExternDeclarations externDeclarations = function.findParentOfType(ExternDeclarations.class);
		if (externDeclarations != null && !"C++".equals(externDeclarations.getLanguage()))
			return;
		
		if (!isCPlusPlusFileName(Element.getFileOfAscendency(function)))
			return;
		
		for (CPlusPlusMangler mangler : result.config.cPlusPlusManglers) {
			try {
				names.add(mangler.mangle(function, result));
			} catch (Exception ex) {
				System.err.println("Error in mangling of '" + function.computeSignature(true) + "' : " + ex);
				ex.printStackTrace();
			}
		}
	}

	private void collectParamComments(Function f) {
		for (Arg arg : f.getArgs()) {
			arg.moveAllCommentsBefore();
			TypeRef argType = arg.getValueType();
			if (argType != null) {
				argType.moveAllCommentsBefore();
				arg.addToCommentBefore(argType.getCommentBefore());
				argType.stripDetails();
			}
			if (arg.getCommentBefore() != null) {
				f.addToCommentBefore("@param " + arg.getName() + " " + Element.cleanComment(arg.getCommentBefore()));
				arg.stripDetails();
			}
		}
	}

	void convertFunctions(List<Function> functions, Signatures signatures, DeclarationsHolder out, Identifier libraryClassName) {
		if (functions != null) {
			//System.err.println("FUNCTIONS " + functions);
			for (Function function : functions) {
				convertFunction(function, signatures, false, out, libraryClassName);
			}
		}
	}

	public Identifier getActualTaggedTypeName(TaggedTypeRef struct) {
		Identifier structName = null;
		Identifier tag = struct.getTag();
		if (tag == null || tag.isPlain() && tag.toString().startsWith("_")) {
			TypeDef parentDef = as(struct.getParentElement(), TypeDef.class);
			if (parentDef != null) {
				structName = new Identifier.SimpleIdentifier(JNAeratorUtils.findBestPlainStorageName(parentDef));
			}
		}
		if (structName == null || structName.toString().equals(""))
			structName = tag;
		return structName == null ? null : structName.clone();
	}
	void convertStruct(Struct struct, Signatures signatures, DeclarationsHolder out, Identifier callerLibraryClass) throws IOException {
		Identifier structName = getActualTaggedTypeName(struct);
		if (structName == null)
			return;
		
		if (struct.isForwardDeclaration())// && !result.structsByName.get(structName).isForwardDeclaration())
			return;
		
		if (!signatures.classSignatures.add(structName))
			return;
		
		Identifier baseClass = ident(struct.getType() == Struct.Type.CUnion ? Union.class : Structure.class);
		
		Struct structJavaClass = publicStaticClass(structName, baseClass, Struct.Type.JavaClass, struct);
		Struct byRef = publicStaticClass(ident("ByReference"), structName, Struct.Type.JavaClass, null, ident(ident(Structure.class), "ByReference"));
		Struct byVal = publicStaticClass(ident("ByValue"), structName, Struct.Type.JavaClass, null, ident(ident(Structure.class), "ByValue"));
		
		final int iChild[] = new int[] {0};
		
		//cl.addDeclaration(new EmptyDeclaration())
		Signatures childSignatures = new Signatures();
		//List<Declaration> children = new ArrayList<Declaration>();
		for (Declaration d : struct.getDeclarations()) {
			if (d instanceof VariablesDeclaration) {
				convertVariablesDeclaration((VariablesDeclaration)d, structJavaClass, iChild, callerLibraryClass);
			} else if (d instanceof TaggedTypeRefDeclaration) {
				TaggedTypeRef tr = ((TaggedTypeRefDeclaration) d).getTaggedTypeRef();
				if (tr instanceof Struct) {
					convertStruct((Struct)tr, childSignatures, structJavaClass, callerLibraryClass);
				} else if (tr instanceof Enum) {
					convertEnum((Enum)tr, childSignatures, structJavaClass, callerLibraryClass);
				}
			} else if (d instanceof TypeDef) {
				TypeDef td = (TypeDef)d;
				TypeRef tr = td.getValueType();
				if (tr instanceof Struct) {
					convertStruct((Struct)tr, childSignatures, structJavaClass, callerLibraryClass);
				} else if (tr instanceof FunctionSignature) {
					convertCallback((FunctionSignature)tr, childSignatures, structJavaClass, callerLibraryClass);
				}
			}
		}
		if (result.config.features.contains(GenFeatures.StructConstructors))
			addStructConstructors(structName, structJavaClass/*, byRef, byVal*/, struct);
		
		structJavaClass.addDeclaration(createAsStructMethod("byReference", byRef));
		structJavaClass.addDeclaration(createAsStructMethod("byValue", byVal));
		structJavaClass.addDeclaration(createAsStructMethod("clone", structJavaClass));
		
		structJavaClass.addDeclaration(decl(byRef));
		structJavaClass.addDeclaration(decl(byVal));
		
		if (result.config.putTopStructsInSeparateFiles && struct.findParentOfType(Struct.class) == null) {
			String library = result.getLibrary(struct);
			Identifier javaPackage = result.getLibraryPackage(library);
			Identifier fullClassName = ident(javaPackage, structJavaClass.getTag().clone());
			
			structJavaClass.removeModifiers(Modifier.Static);
			structJavaClass = result.notifyBeforeWritingClass(fullClassName, structJavaClass, signatures);
			if (structJavaClass != null) {
				PrintWriter pout = result.classOutputter.getClassSourceWriter(fullClassName.toString());
				result.printJavaHeader(javaPackage, pout);
				pout.println(structJavaClass);
				pout.close();
			}
		} else
			out.addDeclaration(decl(structJavaClass));
	}

	private Function createAsStructMethod(String name, Struct byRef) {
		TypeRef tr = typeRef(byRef.getTag().clone());
		Function f = new Function(Function.Type.JavaMethod, ident(name), tr);
		String varName = "s";
		f.addModifiers(Modifier.Public).setBody(block(
			stat(tr.clone(), varName, new Expression.New(tr.clone(), methodCall(null))),
			stat(methodCall(varRef(varName), MemberRefStyle.Dot, "useMemory", methodCall("getPointer"))),
			stat(methodCall("write")),
			stat(methodCall(varRef(varName), MemberRefStyle.Dot, "read")),
			new Statement.Return(varRef(varName))
		));
		return f;
	}

	public void convertStructs(List<Struct> structs, Signatures signatures, DeclarationsHolder out, Identifier libraryClassName) throws IOException {
		if (structs != null) {
			for (Struct struct : structs) {
				if (struct.findParentOfType(Struct.class) != null)
					continue;
					
				convertStruct(struct, signatures, out, libraryClassName);
			}
		}
	}

	public VariablesDeclaration convertVariablesDeclaration(String name, TypeRef mutatedType, int[] iChild, Identifier callerLibraryName, Element... toImportDetailsFrom) throws UnsupportedConversionException {
		name = result.typeConverter.getValidJavaArgumentName(ident(name)).toString();
		//convertVariablesDeclaration(name, mutatedType, out, iChild, callerLibraryName);

		Expression initVal = null;
		TypeRef  javaType = result.typeConverter.convertTypeToJNA(
			mutatedType, 
			TypeConversion.TypeConversionMode.FieldType,
			callerLibraryName
		);
		mutatedType = result.typeConverter.resolveTypeDef(mutatedType, callerLibraryName, true);
		
		VariablesDeclaration convDecl = new VariablesDeclaration();
		convDecl.addModifiers(Modifier.Public);
		
		if (javaType instanceof ArrayRef && mutatedType instanceof ArrayRef) {
			ArrayRef mr = (ArrayRef)mutatedType;
			ArrayRef jr = (ArrayRef)javaType;
			Expression mul = null;
			List<Expression> dims = mr.flattenDimensions();
			for (int i = dims.size(); i-- != 0;) {
				Expression x = dims.get(i);
			
				if (x == null || x instanceof EmptyArraySize) {
					javaType = jr = new ArrayRef(typeRef(Pointer.class));
					break;
				} else {
					Expression c = result.typeConverter.convertExpressionToJava(x, callerLibraryName);
					c.setParenthesis(dims.size() == 1);
					if (mul == null)
						mul = c;
					else
						mul = expr(c, BinaryOperator.Multiply, mul);
				}
			}
			initVal = new Expression.NewArray(jr.getTarget(), mul);
		}
		if (javaType == null) {
			throw new UnsupportedConversionException(mutatedType, "failed to convert type to Java");
		} else if (javaType.toString().equals("void")) {
			throw new UnsupportedConversionException(mutatedType, "void type !");
			//out.add(new EmptyDeclaration("SKIPPED:", v.formatComments("", true, true, false), v.toString()));
		} else {
			for (Element e : toImportDetailsFrom)
				convDecl.importDetails(e, false);
			convDecl.importDetails(mutatedType, true);
			convDecl.importDetails(javaType, true);
			
//			convDecl.importDetails(v, false);
//			convDecl.importDetails(vs, false);
//			convDecl.importDetails(valueType, false);
//			valueType.stripDetails();
			convDecl.moveAllCommentsBefore();
			convDecl.setValueType(javaType);
			convDecl.addDeclarator(new DirectDeclarator(name, initVal));
			
			return convDecl;//out.addDeclaration(convDecl);
		}
	}
	public void convertVariablesDeclaration(VariablesDeclaration v, DeclarationsHolder out, int[] iChild, Identifier callerLibraryClass) {
		//List<Declaration> out = new ArrayList<Declaration>();
		try {
			TypeRef valueType = v.getValueType();
			for (Declarator vs : v.getDeclarators()) {
				String name = vs.resolveName();
				if (name == null || name.length() == 0)
					continue;
	
				TypeRef mutatedType = valueType;
				if (!(vs instanceof DirectDeclarator))
				{
					mutatedType = (TypeRef)vs.mutateType(valueType);
					vs = new DirectDeclarator(vs.resolveName());
				}
				VariablesDeclaration vd = convertVariablesDeclaration(name, mutatedType, iChild, callerLibraryClass, v, vs);
				if (vd != null) {
					Declarator d = v.getDeclarators().get(0);
					if (d.getBits() > 0) {
						int bits = d.getBits();
						vd.addAnnotation(new Annotation(Bits.class, "(" + bits + ")"));
						String st = vd.getValueType().toString(), mst = st;
						if (st.equals("int") || st.equals("long") || st.equals("short") || st.equals("long")) {
							if (bits <= 8)
								mst = "byte";
							else if (bits <= 16)
								mst = "short";
							else if (bits <= 32)
								mst = "int";
							else
								mst = "long"; // should not happen
						}
						if (!st.equals(mst))
							vd.setValueType(new Primitive(mst));
					}
					out.addDeclaration(vd);
				}
				iChild[0]++;
			}
		} catch (UnsupportedConversionException e) {
			if (!result.config.limitComments)
				out.addDeclaration(new EmptyDeclaration(e.toString()));
		}
	}
	TaggedTypeRefDeclaration publicStaticClassDecl(Identifier name, Identifier parentName, Struct.Type type, Element toCloneCommentsFrom, Identifier... interfaces) {
		return decl(publicStaticClass(name, parentName, type, toCloneCommentsFrom, interfaces));
	}
	Struct publicStaticClass(Identifier name, Identifier parentName, Struct.Type type, Element toCloneCommentsFrom, Identifier... interfaces) {
		Struct cl = new Struct();
		cl.setType(type);
		cl.setTag(name);
		if (parentName != null)
			cl.setParents(parentName);
		if (type == Struct.Type.JavaInterface)
			for (Identifier inter : interfaces)
				cl.addParent(inter);
		else
		cl.setProtocols(Arrays.asList(interfaces));
		if (toCloneCommentsFrom != null ) {
			cl.importDetails(toCloneCommentsFrom, false);
			cl.moveAllCommentsBefore();
			cl.addToCommentBefore(getFileCommentContent(toCloneCommentsFrom));
		}
		cl.addModifiers(Modifier.Public, Modifier.Static);
		return cl;
	}
	private void addStructConstructors(Identifier structName, Struct structJavaClass/*, Struct byRef,
			Struct byVal*/, Struct nativeStruct) {
		
		List<Declaration> initialMembers = new ArrayList<Declaration>(structJavaClass.getDeclarations());
		Set<String> signatures = new TreeSet<String>();
		
		Function emptyConstructor = new Function(Function.Type.JavaMethod, structName.clone(), null).addModifiers(Modifier.Public);
		emptyConstructor.setBody(block(stat(methodCall("super"))));
		addConstructor(structJavaClass, emptyConstructor);
		
		
		boolean isUnion = nativeStruct.getType() == Struct.Type.CUnion;
		if (isUnion) {
			Map<String, Pair<TypeRef, List<Pair<String, String>>>> fieldsAndCommentsByTypeStr = new HashMap<String, Pair<TypeRef, List<Pair<String, String>>>>();
			for (Declaration d : initialMembers) {
				if (!(d instanceof VariablesDeclaration))
					continue;
					
				VariablesDeclaration vd = (VariablesDeclaration)d;
				if (vd.getDeclarators().size() != 1)
					continue; // should not happen !
				String name = vd.getDeclarators().get(0).resolveName();
				TypeRef tr = vd.getValueType();
				
				String trStr = tr.toString();
				Pair<TypeRef, List<Pair<String, String>>> pair = fieldsAndCommentsByTypeStr.get(trStr);
				if (pair == null)
					fieldsAndCommentsByTypeStr.put(trStr, pair = new Pair<TypeRef, List<Pair<String, String>>>(tr, new ArrayList<Pair<String, String>>()));
				
				pair.getSecond().add(new Pair<String, String>(vd.getCommentBefore(), name));
			}
			for (Pair<TypeRef, List<Pair<String, String>>> pair : fieldsAndCommentsByTypeStr.values()) {
				List<String> commentBits = new ArrayList<String>(), nameBits = new ArrayList<String>();
				for (Pair<String, String> p : pair.getValue()) {
					if (p.getFirst() != null)
						commentBits.add(p.getFirst());
					nameBits.add(p.getValue());
				}
				String name = StringUtils.implode(nameBits, "_or_");
				TypeRef tr = pair.getFirst();
				Function unionValConstr = new Function(Function.Type.JavaMethod, structName.clone(), null, new Arg(name, tr.clone()));
				if (!commentBits.isEmpty())
					unionValConstr.addToCommentBefore("@param " + name + " " + StringUtils.implode(commentBits, ", or "));
				
				unionValConstr.addModifiers(Modifier.Public);
				
				Expression assignmentExpr = varRef(name);
				for (Pair<String, String> p : pair.getValue())
					assignmentExpr = new Expression.AssignmentOp(memberRef(varRef("this"), MemberRefStyle.Dot, ident(p.getValue())), AssignmentOperator.Equal, assignmentExpr);
				
				unionValConstr.setBody(block(
					stat(methodCall("super")),
					tr instanceof TypeRef.ArrayRef ? throwIfArraySizeDifferent(name) : null,
					stat(assignmentExpr),
					stat(methodCall("setType", result.typeConverter.getJavaClassLitteralExpression(tr)))
				));
				
				if (signatures.add(unionValConstr.computeSignature(false))) {
					structJavaClass.addDeclaration(unionValConstr);
//					byRef.addDeclaration(unionValConstr.clone().setName(byRef.getTag().clone()));
//					byVal.addDeclaration(unionValConstr.clone().setName(byVal.getTag().clone()));
				}
			}
		} else {
			Function fieldsConstr = new Function(Function.Type.JavaMethod, structName.clone(), null);
			fieldsConstr.setBody(new Block()).addModifiers(Modifier.Public);
			fieldsConstr.getBody().addStatement(stat(methodCall("super")));
			for (Declaration d : initialMembers) {
				if (!(d instanceof VariablesDeclaration))
					continue;
					
				VariablesDeclaration vd = (VariablesDeclaration)d;
				if (vd.getDeclarators().size() != 1)
					continue; // should not happen !
				String name = vd.getDeclarators().get(0).resolveName();
				
				if (vd.getCommentBefore() != null)
					fieldsConstr.addToCommentBefore("@param " + name + " " + vd.getCommentBefore());
				fieldsConstr.addArg(new Arg(name, vd.getValueType().clone()));

				if (vd.getValueType() instanceof TypeRef.ArrayRef)
					fieldsConstr.getBody().addStatement(throwIfArraySizeDifferent(name));
				fieldsConstr.getBody().addStatement(stat(new Expression.AssignmentOp(memberRef(varRef("this"), MemberRefStyle.Dot, ident(name)), AssignmentOperator.Equal, varRef(name))));
			}
			int nArgs = fieldsConstr.getArgs().size();
			if (nArgs == 0)
				System.err.println("Struct with no field : " + structName);
			
			if (nArgs > 0 && nArgs < MAX_FIELDS_FOR_VALUES_CONSTRUCTORS) {
				if (signatures.add(fieldsConstr.computeSignature(false))) {
					structJavaClass.addDeclaration(fieldsConstr);
				}
			}
		}
		
//		Function pointerConstructor = new Function(Function.Type.JavaMethod, structName.clone(), null, 
//			new Arg("pointer", new TypeRef.SimpleTypeRef(Pointer.class.getName())),
//			new Arg("offset", new TypeRef.Primitive("int"))
//		).addModifiers(Modifier.Public).setBody(block(
//			stat(methodCall("super", varRef("pointer"), varRef("offset")))
//		).setCompact(true));
//		pointerConstructor.setCommentBefore("Cast data at given memory location (pointer + offset) as an existing " + structName + " struct");
//		pointerConstructor.setBody(block(
//			stat(methodCall("super")),
//			stat(methodCall("useMemory", varRef("pointer"), varRef("offset"))),
//			stat(methodCall("read"))
//		));
//		boolean addedPointerConstructor = false;
//		if (signatures.add(pointerConstructor.computeSignature(false))) {
//			addConstructor(structJavaClass, pointerConstructor);
//			addedPointerConstructor = true;
//		}
		
//		String copyArgName = isUnion ? "otherUnion" : "otherStruct";
//		Function shareMemConstructor = new Function(Function.Type.JavaMethod, structName.clone(), null, 
//			new Arg(copyArgName, new TypeRef.SimpleTypeRef(structName.clone()))
//		).addModifiers(Modifier.Public);
		
//		Block useCopyMem = //addedPointerConstructor ? 
//			//null : 
//			block(
//					stat(methodCall("super")),
//					stat(methodCall("useMemory", methodCall(varRef(copyArgName), MemberRefStyle.Dot, "getPointer"), expr(Constant.Type.Int, 0))),
//					stat(methodCall("read"))
//			)
//		;
//		shareMemConstructor.setBody(//addedPointerConstructor ? 
////			block(
////					stat(methodCall("super", methodCall(varRef(copyArgName), MemberRefStyle.Dot, "getPointer"), expr(Constant.Type.Int, 0)))
////			).setCompact(true) :
//			useCopyMem
//		);
//		shareMemConstructor.setCommentBefore("Create an instance that shares its memory with another " + structName + " instance");
//		if (signatures.add(shareMemConstructor.computeSignature(false))) {
////			addConstructor(byRef, shareMemConstructor);
////			shareMemConstructor = shareMemConstructor.clone();
////			addConstructor(byVal, shareMemConstructor);
//		
//			shareMemConstructor = shareMemConstructor.clone();
//			shareMemConstructor.setBody(/*addedPointerConstructor ?
//				block(
//					stat(methodCall("this", methodCall(varRef(copyArgName), MemberRefStyle.Dot, "getPointer"), expr(Constant.Type.Int, 0)))
//				).setCompact(true) :*/
//				useCopyMem.clone()
//			);
//			addConstructor(structJavaClass, shareMemConstructor);
//		}
	}
	Statement throwIfArraySizeDifferent(String varAndFieldName) {
		return new Statement.If(
			expr(
				memberRef(varRef(varAndFieldName), MemberRefStyle.Dot, "length"), 
				BinaryOperator.IsDifferent,
				memberRef(memberRef(varRef("this"), MemberRefStyle.Dot, varAndFieldName), MemberRefStyle.Dot, "length")
			),
			new Statement.Throw(new Expression.New(typeRef(IllegalArgumentException.class), expr(Constant.Type.String, "Wrong array size !"))),
			null
		);
	}
	void addConstructor(Struct s, Function f) {
		Identifier structName = getActualTaggedTypeName(s);
		
		f.setName(structName);
		s.addDeclaration(f);
	}
	
	String getFileCommentContent(File file, Element e) {
		if (file != null) {
			String path = result.config.relativizeFileForSourceComments(file.getAbsolutePath());
			String inCategoryStr = "";
			if (e instanceof Function) {
				Function fc = (Function)e;
				Struct parent;
				if (fc.getType() == Type.ObjCMethod && ((parent = as(fc.getParentElement(), Struct.class)) != null) && (parent.getCategoryName() != null)) {
					inCategoryStr = "from " + parent.getCategoryName() + " ";
				}
			}
			return "<i>" + inCategoryStr + "native declaration : " + path + (e == null || e.getElementLine() < 0 ? "" : ":" + e.getElementLine()) + "</i>";
		} else if (e != null && e.getElementLine() >= 0) {
			return "<i>native declaration : <input>:" + e.getElementLine() + "</i>";
		}
		return null;
	}
	String getFileCommentContent(Element e) {
		if (result.config.limitComments)
			return null;
		
		String f = Element.getFileOfAscendency(e);
		if (f == null && e != null && e.getElementLine() >= 0)
			return "<i>native declaration : line " + e.getElementLine() + "</i>";
		
		return f == null ? null : getFileCommentContent(new File(f), e);
	}
	

	public List<Define> reorderDefines(Collection<Define> defines) {
		List<Define> reordered = new ArrayList<Define>(defines.size());
		HashSet<Identifier> added = new HashSet<Identifier>(), all = new HashSet<Identifier>();
		
		
		Map<String, Pair<Define, Set<Identifier>>> pending = new HashMap<String, Pair<Define, Set<Identifier>>>();
		for (Define define : defines) {
			Set<Identifier> dependencies = new TreeSet<Identifier>();
			computeVariablesDependencies(define.getValue(), dependencies);
			all.add(ident(define.getName()));
			if (dependencies.isEmpty()) {
				reordered.add(define);
				added.add(ident(define.getName()));
			} else {
				pending.put(define.getName(), new Pair<Define, Set<Identifier>>(define, dependencies));
			}	
		}
		
		for (int i = 3; i-- != 0 && !pending.isEmpty();) {
			for (Iterator<Map.Entry<String, Pair<Define, Set<Identifier>>>> it = pending.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, Pair<Define, Set<Identifier>>> e = it.next(); 
				Set<Identifier> dependencies = e.getValue().getSecond();
				String name = e.getKey();
				boolean missesDep = false;
				for (Identifier dependency : dependencies) {
					if (!added.contains(dependency)) {
						missesDep = true;
						if (!all.contains(dependency)) {
							it.remove();
							all.remove(name);
						}
						
						break;
					}
				}
				if (missesDep)
					continue;
				
				it.remove();
				reordered.add(e.getValue().getFirst());
			}
		}
		
		return reordered;
	}
	public void computeVariablesDependencies(Element e, final Set<Identifier> names) {
		e.accept(new Scanner() {
			@Override
			public void visitSimpleTypeRef(SimpleTypeRef simpleTypeRef) {
				names.add(simpleTypeRef.getName());
			}
		});
	}
	
	private String chooseJavaArgName(String name, int iArg, Set<String> names) {
		String baseArgName = result.typeConverter.getValidJavaArgumentName(ident(name)).toString();
		int i = 1;
		if (baseArgName == null)
			baseArgName = "arg";
		
		String argName;
		do {
			argName = baseArgName + (i == 1 ? "" : i + "");
			i++;
		} while (names.contains(argName) || TypeConversion.isJavaKeyword(argName));
		names.add(argName);
		return argName;
	}

}
