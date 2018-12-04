# Notes
Some notes on language and compiler structure

## AST structure
Levels of AST node:
1. Declaration
2. Statement
3. Expression

## Symbol resolution
Kinds of symbols in order of resolution:

No target:
1. **Local variable**
Structured in nested scopes.
2. **Function**
Resolves to a function reference.
3. **Constructor**
Resolves to a constructor reference
4. **Predefined function**
Same as function

With target:
1. **Class field**
2. **Class function**

## Type system
* Basic types:
   * `int64`
   * `double`
   * `boolean`
* Pointer types: `T*`
   * Array pointer types: `T[]` _(additionally allows shifts)_
* Class types `SomeClass`

## Syntax overview
### Declarations
* Function: `fun someName(arg1: type1, arg2: type2): someType { <body> }`
* Class: `class someName { <declarations> }`
    * Class field: _same as function_
    * Class method: `var someName: someType;`
    
### Statements
* Assignment: `someVar = someValue;`
* If statement: `if (<expr>) { <bodyTrue> } else { <bodyFalse> }`, else branch is optional
* While statement: `while (<expr>) { <body> }`
* Return statement: `return <expr>;`
* Local variable declaration: `var someName: someType;`
* Function call: `someFoo();`

### Expressions
* Integer literal: `12`
* Double literal: `14.0`
* Dereference: `*somePointer`
* Address of: `&someVariable`
* Function call: `someFoo()`
* Parenthesised expression: `(<expr>)`
* Field of class: `someClassVar.someField`
* Binary operations: `+`, `-`, `*`, `/`, `==`, `<`, `&&`, `||`

## X86 compilation notes
### Types representation
1. int64 -- as is
2. boolean -- as int64, `0` if `false`, `1` if `true`