&lt;wiki:gadget url="http://jnaerator.googlecode.com/svn/wiki/adsense468x60.xml" border="0" width="468" height="60" /&gt;

# JNAerator : bring native libs to Java #

**JNAerator** (pronounce _‘generator’_) builds **bridges between native libraries and Java**, leveraging [JNA](http://jna.dev.java.net/) and [Rococoa](https://rococoa.dev.java.net/).

It parses **C**, **C++** and **ObjectiveC** headers, [BridgeSupport](http://bridgesupport.macosforge.org/trac/) files, **Visual Studio** solutions and can even extract and **reverse-engineer DLL exported symbols**.

JNAerator **creates self-contained JARs** with compiled Java proxy definitions and needed runtime + auto-extractible binaries, **ready to use** for Java development.


Everything is **highly automated** : typically you'll just need to pick some headers / DLLs / solutions, but you can also configure many aspects of the process.

**Users who are looking for ready-to-use libraries** should check the [NativeLibs4Java](http://nativelibs4java.googlecode.com/) project.

# Use JNAerator #

Though JNAerator can be used [in command line](CommandLineOptionsAndEnvironmentVariables.md), you might prefer its handy but limited GUI for a first contact :
<table border='0'>
<tr valign='top'><td><a href='http://ochafik.free.fr/Java/JNAeratorStudio.jnlp'><img src='http://jnaerator.googlecode.com/svn/wiki/screenshots/JNAeratorStudio.png' /></a></td>
<td><a href='http://ochafik.free.fr/Java/JNAeratorStudio.jnlp'>JNAeratorStudio (direct Web Start Link)</a>

<a href='http://ochafik.free.fr/Java/JNAeratorStudio.jnlp'><img src='http://jnaerator.googlecode.com/svn/wiki/launch.jpg' /></a>

<a href='http://sourceforge.net/donate/index.php?group_id=266856'><img src='http://images.sourceforge.net/images/project-support.jpg' alt='Support This Project' border='0' width='88' height='32' /> </a>
</td>
</tr></table>

# Features #

TODO

# Limitations #

C++ runtime support is limited to functions, static methods and globals (no instance methods nor object creation possible yet).

Symbols extraction from shared libraries is limited to Windows DLLs for now, and is only available when run from Windows.

Also, JNAerator's parser may not parse well everything you may want to throw at it. Please file bug reports in the Issues section if you manage to isolate parsing bugs.