

# Overview #

<a href='http://sourceforge.net/donate/index.php?group_id=266856'><img src='http://images.sourceforge.net/images/project-support.jpg' alt='Support This Project' border='0' width='88' height='32' /> </a>
See the [list of donors here](http://code.google.com/p/bridj/wiki/CreditsAndLicense).

JNAerator's parser and transformation code is Copyright [Olivier Chafik](http://ochafik.free.fr/) (c) 2008-2010 (everything that lies in the `com.ochafik.*`, `com.bridj.*` and `com.nativelibs4java.*` packages]).

JNAerator ships with the following libraries :
  * [BridJ](http://bridj.googlecode.com/) (BSD license, Copyright (c) 2009-2010, Olivier Chafik) : A new C/C++ interop library akin to JNA
  * [Anarres JCPP](http://www.anarres.org/projects/jcpp/) (Apache 2.0 license, Copyright (c) 2007-2008, Shevek) : [patched with a few fixes and enhancements (all contributed back)](http://code.google.com/p/nativelibs4java/source/browse/trunk/libraries/jnaerator/anarres/)
  * [Java Native Access (JNA)](http://jna.dev.java.net/) (LGPL license, Copyright (c) 2006-2009 Todd Fast, Timothy Wall, Wayne Meissner & others) : [patched](http://code.google.com/p/nativelibs4java/source/browse/trunk/libraries/jnaerator/jna-jnaerator/src/main/java/com/sun/jna/Structure.java) with support for bit fields in structs
  * [Rococoa](https://rococoa.dev.java.net/) (LGPL license, Copyright Duncan McGregor and others, see below) : [slightly modified to make NSObject an abstract class](http://ochafik.free.fr/nativelibs4java/rococoa-jnaerator.zip)
  * [ANTLR's runtime](http://www.antlr.org/) (BSD license, Copyright (c) 2003-2008, Terence Parr) : unmodified
  * [jEdit](http://jedit.org/) 2.2.1 syntax highlighting package ([released to the public domain](http://www.arachnoid.com/arachnophilia/Documentation/README.txt), Copyright 1998-1999 Slava Pestov, Artur Biesiadowski, Clancy Malcolm, Jonathan Revusky, Juha Lindfors and Mike Dillon)
  * [JaLiCo](http://jalico.googlecode.com/), also written by Olivier Chafik (Apache 2.0 license).
  * [JUnit 4.4](http://www.junit.org) ([CPL license](http://junit.sourceforge.net/cpl-v10.html)), Copyright Kent Beck, Erich Gamma, and David Saff
  * [Eclipse standalone compiler 3.5](http://download.eclipse.org/eclipse/downloads/drops/S-3.5RC4-200906051444/index.php#JDTCORE). It is released under the [Eclipse Public License 1.0](http://www.eclipse.org/legal/epl-v10.html) and its source is available on Eclipse's website.

Please read the following sections for licensing details related to the aforementioned libraries.

Note that in accordance with the LGPL library, you should be able to use another version of JNA (possibly modified or upgraded) instead of that shipping with JNAerator. Simply make sure its JAR appears first in the classpath, or replace the com.sun.jna.`*` packages inside JNAerator.jar by the new ones.

You might also be interested in seeing [who uses JNAerator](ProjectsUsingJNAerator.md).

# Anarres JCPP #

Copyright (c) 2007-2008, Shevek

[Anarres JCPP](http://www.anarres.org/projects/jcpp/) is a Java C PreProcessor licensed under the [Apache 2.0 license](https://svn.anarres.org/svn/repos/code/java/cpp/trunk/LICENSE)

# JNA #

Copyright (c) 2006-2009 Todd Fast, Timothy Wall, Wayne Meissner & others

The [Java Native Access (JNA)](http://jna.dev.java.net/) library is distributed under the terms of the [LGPL license](http://www.gnu.org/copyleft/lesser.html).

[Its sources are available here](https://jna.dev.java.net/servlets/ProjectDocumentList).

# Rococoa #

Copyright (c) Duncan McGregor, Harald Kuhr, David Kocher and [others](https://rococoa.dev.java.net/rococoa-acknowledgements.html)

The [Rococoa](http://rococoa.dev.java.net/) library is distributed under the terms of the [LGPL license](http://www.gnu.org/copyleft/lesser.html).

[Its sources are available here](https://rococoa.dev.java.net/servlets/ProjectDocumentList), and the [modified sources used in JNAerator are available here](http://ochafik.free.fr/nativelibs4java/rococoa-jnaerator.zip).

# ANTLR Runtime #

[ANTLR 3](http://www.antlr.org/) and its runtime library are released under [the following license](http://www.antlr.org/license.html) :

`[`The BSD License`]`
Copyright (c) 2003-2008, Terence Parr
All rights reserved.
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
Neither the name of the author nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

# JUnit 4.4 #

[JUnit 4.4](http://www.junit.org) is written by Kent Beck, Erich Gamma, and David Saff and released under the [Common Public License](http://junit.sourceforge.net/cpl-v10.html).