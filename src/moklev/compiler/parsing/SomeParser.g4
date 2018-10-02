parser grammar SomeParser;

options {
    tokenVocab = SomeLexer;
}

@header {
    import moklev.compiler.ast.*;
    import moklev.compiler.ast.impl.*;
    import java.util.stream.Collectors;
}

file returns [DeclarationListNode result]
    : declarations+=declaration* { $result = new DeclarationListNode($declarations.stream().map(x -> x.result).collect(Collectors.toList())); }
    ;
    
declaration returns [DeclarationASTNode result]
    : functionDeclaration { $result = $functionDeclaration.result; }
    ;
    
functionDeclaration returns [FunctionDeclarationNode result]
    : 'fun' name=IDENT '(' (| list+=parameter (',' list+=parameter)* ) ')' ':' returnType=IDENT '{' body=statementList '}'
        { $result = new FunctionDeclarationNode(
            $name.text, 
            $list.stream().map(x -> new kotlin.Pair<>(x.name.getText(), x.type.getText())).collect(Collectors.toList()),
            $returnType.text,
            $body.result
          ); 
        }
    ;
    
statementList returns [StatementListNode result] locals [List<StatementASTNode> list]
    : statements+=statement* { 
        $list = new ArrayList<>();
        for (int i = 0; i < $statements.size(); i++) {
            $list.add($statements.get(i).result);
        }
        $result = new StatementListNode($list);
     }
    ;
    
statement returns [StatementASTNode result]
    : lhs=expression '=' rhs=expression ';' { $result = new AssignmentNode($lhs.result, $rhs.result); }
    | 'if' '(' cond=expression ')' '{' bodyTrue=statementList '}' ('else' '{' bodyFalse=statementList '}')? 
        { $result = new IfNode($cond.result, $bodyTrue.result, $bodyFalse.ctx == null ? new StatementListNode(new ArrayList<>()) : $bodyFalse.result); }
    | 'while' '(' cond=expression ')' '{' body=statementList '}'
        { $result = new WhileNode($cond.result, $body.result); }
    | 'return' value=expression ';' { $result = new ReturnNode($value.result); }
    | 'var' name=IDENT ':' type=IDENT ';' { $result = new VariableDeclarationNode($name.text, $type.text); }
    ;
    
expressionList returns [List<ExpressionASTNode> result]
    : expr=expression (',' list+=expression)* {
        $result = new ArrayList<>();
        $result.add($expr.result);
        for (int i = 0; i < $list.size(); i++) {
            $result.add($list.get(i).result);
        }
      }
    | { $result = new ArrayList<>(); }
    ;
    
expression returns [ExpressionASTNode result]
    : name=IDENT { $result = new SymbolNode($name.text); }
    | value=NUMBER { $result = new ConstantNode($value.text); }
    | target=expression '(' list=expressionList ')' { $result = new InvocationNode($target.result, $list.result); }
    | left=expression op='+' right=expression { $result = new BinaryOperationNode($op.text, $left.result, $right.result); }
    | left=expression op=('==' | '<') right=expression { $result = new BinaryOperationNode($op.text, $left.result, $right.result); }
    ; 
    
parameter
    : name=IDENT ':' type=IDENT
    ;