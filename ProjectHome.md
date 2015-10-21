&lt;wiki:gadget url="http://jnaerator.googlecode.com/svn/wiki/adsense468x60.xml" border="0" width="468" height="60" /&gt;

# JNAerator : bring native libs to Java #

<i>Moving to <a href='http://github.com/nativelibs4java/JNAerator'>github.com/nativelibs4java/JNAerator</a></i>
<br />

**JNAerator** (pronounce _‘generator’_) simply **parses C / C++ & Objective-C** headers and **generates the corresponding [BridJ](http://bridj.googlecode.com), [JNA](http://jna.dev.java.net/) and [Rococoa](https://rococoa.dev.java.net/)** Java interfaces.

This lets Java programmers access native libraries transparently, with full IDE support and little to no hand-tweaking.

**Users who are looking for ready-to-use libraries** should check the [NativeLibs4Java](http://nativelibs4java.googlecode.com/) project.

**_Just. Forget. JNI._**

It can be used [in command line](CommandLineOptionsAndEnvironmentVariables.md) or through its handy but limited GUI :
<table border='0'>
<tr valign='top'><td><a href='http://nativelibs4java.sourceforge.net/webstart/jnaerator/JNAeratorStudio.jnlp'><img src='http://jnaerator.googlecode.com/svn/wiki/screenshots/JNAeratorStudio.png' /></a></td>
<td><a href='http://nativelibs4java.sourceforge.net/webstart/jnaerator/JNAeratorStudio.jnlp'>JNAeratorStudio (direct Web Start Link)</a>

<a href='http://nativelibs4java.sourceforge.net/webstart/jnaerator/JNAeratorStudio.jnlp'><img src='http://jnaerator.googlecode.com/svn/wiki/launch.jpg' /></a>


<a href='http://sourceforge.net/donate/index.php?group_id=266856'><img src='http://images.sourceforge.net/images/project-support.jpg' alt='Support This Project' border='0' width='88' height='32' /> </a>
</td>
</tr></table>

# Features #

(see [Roadmap](Roadmap.md) for planned features)

  * Parses C, C++, Objective-C headers :
    * C preprocessor syntax (using [Anarres JCPP](http://www.anarres.org/projects/jcpp/))
    * GCC and Microsoft VC++ syntax extensions
    * [structs, unions and classes](http://code.google.com/p/jnaerator/wiki/JNAerationResults#Structs)
    * [functions](http://code.google.com/p/jnaerator/wiki/JNAerationResults#Functions), [callbacks](http://code.google.com/p/jnaerator/wiki/JNAerationResults#Callbacks_fields) and methods
    * typedefs, constants, enums and [global variables](http://code.google.com/p/jnaerator/wiki/JNAerationResults#Global_Variables)...
    * **Beware** : STL and Boost are still (very) glitchy
  * Creates the following wrappers
    * C / C++ wrappers for [BridJ](http://code.google.com/p/bridj/)
    * C wrappers for [JNA](https://jna.dev.java.net/) (including for the [experimental direct mode](https://github.com/twall/jna/blob/master/www/DirectMapping.md))
    * Objective-C wrappers for [Rococoa](https://rococoa.googlecode.com/) (including protocols, [BridgeSupport](http://bridgesupport.macosforge.org/trac/) files)
  * Parses Visual Studio solutions and knows about standard compilers and include paths
  * Retains original comments and converts them to JavaDocs

# Quick Start #

Take a look at the [Documentation](Documentation.md)...

Then launch [JNAeratorStudio (direct Web Start Link)](http://jnaerator.sourceforge.net/webstart/JNAerator/JNAeratorStudio.jnlp) and start converting your C/Objective-C headers straight away !

# News, Announcements, Discussions #

[News and announcements are made on the author's blog](http://ochafik.com/).

Discussions and support take place on [JNA's users mailing list](https://groups.google.com/forum/#!forum/jna-users) or on [NativeLibs4Java's mailing list](https://groups.google.com/forum/#!forum/nativelibs4java).

Bug reports and requests for enhancements should be filed in the [Issues section](http://code.google.com/p/jnaerator/issues/list).

# Credits and License #

JNAerator is written and maintained by Olivier Chafik and is distributed under the terms of the LGPL license, but it relies on many great thirdparty libraries.

Please read the [CreditsAndLicense](CreditsAndLicense.md) page for detailed Copyright notices and licenses.