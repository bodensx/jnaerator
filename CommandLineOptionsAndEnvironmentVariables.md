

# JNAerator Files #

As the number of arguments needed for a project might quickly grow and produce insane command lines, you might consider putting some or all of your arguments in a `*.jnaerator` file (preferably, `config.jnaerator`).

Please read [JNAeratorFiles](JNAeratorFiles.md) for more details about these files, and [JNAeratorFAQ](JNAeratorFAQ.md) for more general questions.

# Syntax #

` java -jar jnaerator.jar options headerFiles dynamicLibrariesFiles `

# Options #

  *  file
> > Any header (or directory containing headers at any level of hierarchy), shared library, `*`.bridgesupport file or `*`.jnaerator file
> > _file_: OptionalFile
  * **-noComp**
> > (deprecated) Do not compile JNAerated headers
  * **-noJar**
> > (deprecated) Do not create an output JAR
  * **-noLibBundle**
> > Do not bundle libraries in output JAR
  * **-h**
  * **-help**
> > Show command line arguments help
  * **-Dname=value**
  * **-D name value**
  * **-Dname=**
  * **-DmacroName(x, y, z)=macroBody**
> > Define a macro symbol
> > _name_: String
> > _value_: String
  * **-Fpath**
  * **-F path**
> > Add a directory to the frameworks path. See doc of JNAERATOR\_FRAMEWORKS\_PATH
> > _path_: File
  * **-Ipath**
  * **-I path**
> > Add a directory to the include path or include a file. See doc of JNAERATOR\_INCLUDE\_PATH
> > _path_: File
  * **-Mname=value**
  * **-M name value**
  * **-Mname=**
> > Define an implicit macro symbol, as if it were added by the system (won't count as an explicit macro when generating GYP files and other build artifacts)
> > _name_: String
> > _value_: String
  * **-Tname=value**
  * **-T name value**
  * **-Tname=**
> > Define a type symbol
> > _name_: String
> > _value_: String
  * **-Uname**
  * **-U name**
> > Undefine a preprocessor symbol after the autoconfiguration phase.
> > _name_: String
  * **-addRootDir** path
> > Remove this directory from the path of descendant source files in the generated documentation.
> > _path_: ExistingDir
  * **-allowedFileExts** extensions
> > Colon-separated list of file extensions used to restrict files used when recursing on directories, or "`*`" to parse all files (by default = h:hpp:hxx)
> > _extensions_: String
  * **-arch** archName
> > Define the current architecture for libraries (state variable)
> > _archName_: linux\_x64 | linux\_x86 | armeabi | sunos\_x86 | sunos\_sparc | darwin\_universal | win32 | win64
  * **-beanStructs**
> > Generate getters and setters for struct fields (JNA & JNAerator runtimes only)
  * **-beautifyNames**
> > Transform C names to Java-looking names : some\_func() => someFunc()
  * **-bridgeSupportOut** outFile
> > Write the definitions extracted from bridgesupport files in a file (automatically set when ${Verbose} is used).
> > _outFile_: OutputFile
  * **-callbacksInvokeMethodName** methodName
> > Name of the invocation method of callbacks ('apply' by default)
> > _methodName_: String
  * **-choices** choicesFile
> > Read the function alternative choices from a file in the format used by -choicesOut.
> > _choicesFile_: ExistingFile
  * **-choicesOut** outFile
> > Write the function alternative choices made (automatically set when ${Verbose} is used).
> > _outFile_: OutputFile
  * **-com**
> > Generate Microsoft COM (C++) bindings.
  * **-convertBodies**
> > Experimental conversion of function bodies to equivalent Java code (BridJ only).
  * **-defaultLibrary** libName
> > Name of output library for elements declared in files not covered by a ${CurrentLibrary} switch
> > _libName_: String
  * **-dependencies** lib1,lib2
> > Comma-separated list of dependencies for the current library (BridJ only).
> > _methodName_: CommaList
  * **-direct**
> > JNAerate libraries that use JNA's faster direct call convention
  * **-dontCastConstants**
> > Don't cast generated constants
  * **-emptyStructsAsForwardDecls**
> > Treat empty structs as forward declarations
  * **-entryClass** entryClassName
> > Generate a class _entryclassName.EntryClassName_ that will contain all of the jnaerated libraries instances. User code will just need to static import or derive from this class to access to the instances (has no effect for BridJ runtime).
> > _entryClassName_: String
  * **-extractDeclarations** interfaceSimpleClassName
> > Extract current library's declarations to an interface with the provided simple name (will be in same package as library implementation class); BridJ-specific
> > _interfaceSimpleClassName_: String
  * **-extractionOut** outFile
> > Write the symbols extracted from libraries in a file (automatically set when ${Verbose} is used).
> > _outFile_: OutputFile
  * **-f**
> > Force the overwrite of existing files
  * **-forceNames**
> > Force @Name annotations on all supported entities (structs, classes, enums, methods) to allow refactoring of resulting sources (BridJ-only).
  * **-forceStringSignatures**
> > Force String and String[.md](.md) signatures for char`*` and char`*``*` params (JNA runtime only).
  * **-fpreprocessed**
> > Consider source files as being already preprocessed (preprocessor won't be run)
  * **-framework** frameworkName
> > JNAerate a framework using its headers and its `*`.bridgesupport files if available
> > _frameworkName_: String
  * **-frameworksPath** path1:path2...
> > See doc of JNAERATOR\_FRAMEWORKS\_PATH
> > _path1:path2..._: String
  * **-gccLong**
> > Use GCC convention for size of 'long' (4 bytes on 32 bits platforms, 8 bytes on 64 bits platforms).
  * **-genCPlusPlus**
> > [Experimental, Not working at all] Generate C++ classes.
  * **-genPrivateMembers**
> > Generate wrappers for private fields and methods (will be protected and deprecated).
  * **-gui**
> > Show minimalist progression GUI
  * **-ifRegexMatch** javaProperty regex thenArg elseArg
> > Conditional evaluation of an argument if a java system property matches a regular expression
> > _javaProperty_: String
> > _regex_: String
> > _thenArg_: String
> > _elseArg_: String
  * **-jar** outFile
> > Jar file where all generated sources and the compiled classes go
> > _outFile_: OutputFile
  * **-libFile** resourceFile
> > Bundle the provided file with the JNAerated JAR so that it is extracted with the library when it is first used.
> > _resourceFile_: ExistingFile
  * **-library** libName
> > Define the name of the output library. This is a state parameter, it will affect all files listed after it, until another -library switch is provided. It does not affect sources included from a project file (Visual Studio...).
> > C functions exported in library "test" will end up in class "TestLibrary", for instance.
> > The name of the library is the one fed to JNA to find the shared library, so library "test" must be in "test.dll" on Windows, "libtest.dylib" on Mac OS X and  "libtest.so" on other Unices.
> > Note that a special hack is done for library "c" on Windows systems : the output name is set to "msvcrt" instead of "c".


> _libName_: String
  * **-libraryNamingPrefixes** commaSeparatedPrefixes
> > Define prefixes commonly used in the library so that reification of functions is optimal (See -reification)
> > _commaSeparatedPrefixes_: String
  * **-libraryOverrides** list
> > Comma-separated list of `symbol=library` library overrides (when isolated functions are located in a different library than their surrounding code).
> > _list_: Map
  * **-limitComments**
> > Avoid useless comments (source file + line, skipped items...)
  * **-macrosOut** outFile
> > Write the preprocessor macros in a file (automatically set when ${Verbose} is used).
> > _outFile_: OutputFile
  * **-mavenArtifactId** artifactId
> > Set artifact id of the generated Maven project
> > _artifactId_: String
  * **-mavenGroupId** groupId
> > Set group id of the generated Maven project
> > _groupId_: String
  * **-mavenVersion** version
> > Set version of the generated Maven project
> > _version_: String
  * **-maxConstrFields** fieldCount
> > Maximum number of fields allowed for structure fields constructors. If a struct has more fields, it will only get a default constructor.
> > _fieldCount_: Int
  * **-mode** mode
> > Choose the output mode of JNAerator
> > _mode_: 'Jar' : JAR with bindings only | 'StandaloneJar' : JAR with bindings and runtime dependencies | 'Directory' : Bindings sources in simple file hierarchy | 'Maven' : Bindings sources in Maven project ready to build | 'AutoGeneratedMaven' : Maven project that automatically regenerates its bindings
  * **-noAuto**
> > No auto-configuration of preprocessor symbols and paths
  * **-noAutoImport**
> > Don't add import statements automatically to output java source files
  * **-noComments**
> > Don't output any member comment.
  * **-noMangling**
> > Don't output any C++ name mangling information (may cause C++-decorated symbols not to be found at execution time).
  * **-noPrimitiveArrays**
> > Never output primitive arrays for function arguments (use NIO buffers instead)
  * **-noRawBindings**
> > Don't generate raw bindings amenable for assembler optimizations.
  * **-noStaticInit**
> > Don't generate static init block with BridJ.register() call (BridJ-specific)
  * **-noStringReturns**
> > Prevent const char`*` and const wchar\_t`*` return types from being converted to String and WString.
  * **-nocpp**
> > Do not define the cplusplus symbol
  * **-o** outDir
> > Output directory for all artifacts
> > _outDir_: OutputDir
  * **-onlineDoc** linkDisplayFormat urlMessageFormat
> > Define a format for online documentation URLs (uses MessageFormat syntax, with arg 0 being the name of the function / structure).
> > _linkDisplayFormat_: MessageFormat
> > _urlMessageFormat_: MessageFormat
  * **-optionalFunctions** namePattern
> > Mark functions which name matches the provided regular expression pattern with the @Optional annotation (BridJ-only)
> > _namePattern_: String
  * **-package** forcedPackageName
> > Set the Java package in which all the output will reside (by default, set to the library name).
> > _forcedPackageName_: String
  * **-parseInOnePiece**
> > Doesn't split the pre-processor output into multiple smaller parts and parse everything as it should (in theory everything should be parsed in one chunk, but in practice this means errors are not isolated from the rest of the parsing)
  * **-preferJavac**
> > Use Sun's Javac compiler instead of Eclipse's ecj, if possible
  * **-preprocessingOut** outFile
> > Write the preprocessor output in a file (automatically set when ${Verbose} is used).
> > _outFile_: OutputFile
  * **-project** solutionFile "Config|Platform"
> > Read Visual Studio 2008 project or solution file and use the configuration specified (e.g. "Release|Win32").
> > _solutionFile_: ExistingFile
> > _"Config|Platform"_: String
  * **-publicRawBindings**
> > Make raw bindings public.
  * **-reification**
> > Automatically create OO shortcuts for functions that look like methods (typedPtr.someFunc() for someFunc(typedPtr))
  * **-removeInlineAsm**
> > Remove inline asm from preprocessed source, useful when its unsupported syntax makes parsing to fail.
  * **-root**
  * **-rootPackage**
> > Define the root package for all output classes
> > _package_: String
  * **-runtime** enum
> > Choose target runtime library between JNA, JNAerator (based on JNA), BridJ (faster runtime that supports C++), NodeJS (experimental native library bindings for node.js) (default: BridJ (faster runtime that supports C++)).
> > _enum_: JNA | JNAerator (based on JNA) | BridJ (faster runtime that supports C++) | NodeJS (experimental native library bindings for node.js)
  * **-scalaOut** outDir
> > [Experimental](Experimental.md) Output Scala wrappers (callbacks implicits...)
> > _outDir_: OutputDir
  * **-scalaStructSetters**
> > Generate Scala-style setters for BridJ structs (with a name like fieldName_$eq)
  * **-scanSymbols**
> > Extract, unmangle and parse the symbols all listed shared libraries
  * **-sizeAsLong**
> > Treat size\_t and ptrdiff\_t values as 'long' values. ONLY HERE FOR COMPATIBILITY WITH PREVIOUS VERSIONS, WILL EVENTUALLY BE REMOVED.
  * **-skipDeprecated**
> > Don't generate members that would be tagged as @Deprecated
  * **-skipEnums** namePattern
> > Skip enumerations which name matches the provided regular expression pattern
> >_namePattern_: String
  * **-skipFunctions** namePattern
> > Skip functions which name matches the provided regular expression pattern
> >_namePattern_: String
  * **-skipIncludedFrameworks**
> > Skip Included Frameworks
  * **-skipLibraryInstance**
> > Skip library instance declarations
  * **-skipStructs** namePattern
> > Skip structs and classes which name matches the provided regular expression pattern
> >_namePattern_: String
  * **-structsInLibrary**
> > Force structs to be JNAerated as inner classes of their declaring libraries (otherwise, each top-level structure is defined as a top-level class in its library's package)
  * **-studio**
> > Launch JNAeratorStudio
  * **-synchronized**
> > Generate synchronized native methods
  * **-v**
  * **-verbose**
> > Verbose output (both console and files)
  * **-wcharAsShort**
> > Force treatment of wchar\_t as short (char by default)
  * **-wikiHelp**
> > Output a wiki-friendly help
  * **@path**
  * **@ path**
> > Read command-line arguments from a file. File may contain multiple lines (those beginning with "//" will be skipped), file wildcards will be resolved within the file content, as well as variables substitutions : $(someEnvOrJavaVarName), with $(DIR) being the parent directory of the current arguments file.
> >_argumentsFile.jnaerator_: ExistingFile
> > Read command-line arguments from a file. File may contain multiple lines (those beginning with "//" will be skipped), file wildcards will be resolved within the file content, as well as variables substitutions : $(someEnvOrJavaVarName), with $(DIR) being the parent directory of the current arguments file._

# Environment Variables #

All of these variables may be overridden by setting the environment variable (` set VAR=value ` on Windows, ` export VAR=value ` on most unices) or through Java properties (` java -DVAR=value -jar jnaerator.jar ... `).

  * VISUAL\_STUDIO\_HOME =
```
C:\Program Files\Microsoft Visual Studio 9.0 
```
  * WINDOWS\_SDK\_HOME
```
C:\Program Files\Microsoft SDKs\Windows\v6.0A
```
  * VISUAL\_STUDIO\_INCLUDES = VISUAL\_STUDIO\_HOME;WINDOWS\_SDK\_HOME
  * JNAERATOR\_INCLUDE\_PATH : has the following platform-dependent default values :
    * Windows : uses VISUAL\_STUDIO\_INCLUDES
    * Mac OS X : ` /Developer/SDKs/MacOSX10.4u.sdk/usr/include:. `
    * Current dir on any other platform

  * JNAERATOR\_FRAMEWORKS\_PATH is only set on Mac OS X :
```
/System/Library/Frameworks/CoreServices.framework/Versions/Current/Frameworks:\
/System/Library/Frameworks/ApplicationServices.framework/Versions/Current/Frameworks:\
/System/Library/Frameworks:\
/Library/Frameworks:\
~/Library/Frameworks 
```
  * library.Xxx (Java property) or XXX\_LIBRARY (env. variable) : path to the binary  of library Xxx.