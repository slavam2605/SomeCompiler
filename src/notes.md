# Notes
Some notes on language and compiler structure

## AST structure
Levels of AST node:
1. Declaration
2. Statement
3. Expression

## Symbol resolution
Kinds of symbols in order of resolution:
1. **Local variable**
Structured in nested scopes.
2. **Function**
Resolves to a function reference.

## Type system
* Basic types:
   * `Int64`
   * `Double`
   * `Boolean`
* Pointer types: `T*`
   * Array pointer types: `T[]` _(additionally allows shifts)_