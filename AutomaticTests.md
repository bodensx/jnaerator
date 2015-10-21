

# Overview #

There are many things to test in JNAerator, and the current test coverage is rather low.

All the tests can be launched by [ObjCppTests.java](http://code.google.com/p/jnaerator/source/browse/trunk/sources/com/ochafik/lang/jnaerator/parser/ObjCppTests.java) (uses a custom JUnit test aggregator that needs JUnit 4.4 - **won't work with JUnit 4.5**), or from the command line :
```
java -jar jnaerator.jar -test
```


# JNAerated Artifacts Compilation Tests #

These are launched by [JNAerationTests.java](http://code.google.com/p/jnaerator/source/browse/trunk/sources/com/ochafik/lang/jnaerator/JNAerationTests.java) and operate on the `*`.test files in the [JNAerator tests folder](http://code.google.com/p/jnaerator/source/browse/trunk/sources/com/ochafik/lang/jnaerator/tests).

# DOM Elements tests #

These tests are run by [ObjCppElementsTests.java](http://code.google.com/p/jnaerator/source/browse/trunk/sources/com/ochafik/lang/jnaerator/parser/ObjCppElementsTests.java) and make heavy use of reflection.

There are tests that ensure that all subclasses of the Element class behave as Java beans and respect the hierarchy :
  * any child element added to a parent element must appear in one of the properties of its parent (or must be one of the items in a list or set property), and the child's parentElement property must point to the parent.
  * any element can be instantiated with a default constructor
  * get(set) of any property should return an object that is equal to the one provided (equal with the Object.equals method)
  * getters of lists should be unmodifiable (there are addXXX methods to add items to list or set properties)
  * any element of any type added to any other element of any type as some compatible property must be visited by a Scanner.

# Parsing Tests #

Parsing tests are executed by [ObjCppParsingTests.java](http://code.google.com/p/jnaerator/source/browse/trunk/sources/com/ochafik/lang/jnaerator/parser/ObjCppParsingTests.java) and their data is stored in [ObjCppTest.mm](http://code.google.com/p/jnaerator/source/browse/trunk/sources/com/ochafik/lang/jnaerator/parser/ObjCppTest.mm)

I ensure that a few dozen expressions are parsed without error and that they are serialized back exactly as they were read.

I also ensure some expressions are not parsed properly (e.g. ` int[4] var; `.

Some expressions are not yet serialized back properly and I only test that their parsing yields no error.