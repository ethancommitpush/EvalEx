ExBuilder - A builder to create customized EvalEx Expression 
==========

### Introduction

ExBuilder is a handy expression builder for adding your own methods to the Expression object.

Key Features:
- Define the builder once, and reuse it the build Expression in a convenient way

### Usage Examples

````java
 ExpressionBuilder builder = 
 		(new ExpressionBuilder())
 		.setMathContext(mc)
 		.setVariableMap(variableMap)
 		.setOperatorMap(operatorMap)
 		.setFunctionMap(functionMap);
 Expression expression1 = builder.setExpression("'addr='+replace('192.168.0.1','0','19')").build();
 ExpNode result1 = expression1.eval();
 String strResult1 = result1.getText();
 Expression expression2 = builder.setExpression("'ping result='+ping('127.0.0.1',1000)").build();
 ExpNode result2 = expression2.eval();
 String strResult2 = result2.getText();
````

### Add Custom Variables, Operators, and Functions 

Usage example in the TestExpression.java file
````

### Project Layout

The software was created and tested using Java 1.6.0.

    src/   The Java sources
    test/  JUnit tests
  
### Author and License

Copyright 2018 by Yisin Lin

https://github.com/ethancommitpush
