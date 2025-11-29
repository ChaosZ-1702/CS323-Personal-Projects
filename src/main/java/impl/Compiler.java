package impl;

import framework.AbstractCompiler;
import framework.AbstractGrader;
import framework.lang.Type;
import framework.project3.Project3SemanticError;
import framework.project4.Project4Exception;
import framework.project4.Project4SemanticError;
import generated.Splc.SplcBaseVisitor;
import generated.Splc.SplcLexer;
import generated.Splc.SplcParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.IOException;
import java.util.*;

public class Compiler extends AbstractCompiler {
    public Compiler(AbstractGrader grader) {
        super(grader);
    }

    @Override
    public void start() throws IOException {
        CharStream input = CharStreams.fromStream(this.grader.getSourceStream());
        SplcLexer lexer = new SplcLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SplcParser parser = new SplcParser(tokens);

        // TODO: XXX
        SplcParser.ProgramContext program = parser.program();

//        new SplcBaseVisitor<Void>() {
//            // These are merely examples to show how to create and report a Semantic Error.
//            // The Alternative name (ExprID, VarDecBase, etc.) used here may not be the same as yours.
//            // So it's fine that this code won't compile. You are free to delete all of these code.
//            @Override
//            public Void visitExprID(SplcParser.ExprIDContext ctx) {
//                var ident = ctx.Identifier();
//                grader.reportSemanticError(Project3SemanticError.undeclaredUse(ident));
//                return null;
//            }
//
//            @Override
//            public Void visitVarDecBase(SplcParser.VarDecBaseContext ctx) {
//                var ident = ctx.Identifier();
//                grader.reportSemanticError(Project3SemanticError.redefinition(ident));
//                return null;
//            }
//        }.visit(program);

        myVisitor v = new myVisitor();
        v.visit(program);

        // for project 4, we don't need the outputs in project 3...
//        for (Map.Entry<String, Type> vs : v.variables.entrySet())
//            if (vs.getValue() instanceof StructureType st) {
//                if (!st.isComplete && v.incompleteIdentifiers.containsKey(vs.getKey()))
//                    grader.reportSemanticError(Project3SemanticError.definitionIncomplete(v.incompleteIdentifiers.get(vs.getKey())));
//            }
//
//        grader.print("Variables:\n");
//            for (Map.Entry<String, Type> vs : v.variables.entrySet())
//                grader.print(vs.getKey() + ": " + vs.getValue().fullPrint() + "\n");
//
//            grader.print("\n");
//
//            grader.print("Functions:\n");
//            for (Map.Entry<String, FunctionType> vs : v.functions.entrySet())
//                grader.print(vs.getKey() + ": " + vs.getValue().prettyPrint() + "\n");
    }

    // Type Systemmmmmmmmm
    private static class PrimitiveType implements Type {
        String name;

        public PrimitiveType(String name) {
            this.name = name;
        }
        @Override
        public String prettyPrint() {
            return this.name;
        }
        // Project 4 -- Type Equality
        public boolean equals(Object t) {
            return t instanceof PrimitiveType && this.name.equals(((PrimitiveType) t).name);
        }
    }

    private static class FunctionType implements Type {
        Type returnType;
        ArrayList<Type> parameterTypes;

        public FunctionType(Type returnType, ArrayList<Type> parameterTypes) {
            this.returnType = returnType;
            this.parameterTypes = parameterTypes;
        }
        @Override
        public String prettyPrint() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.returnType.prettyPrint()).append("(");
            for (int i = 0; i < this.parameterTypes.size(); i++) {
                if (i != this.parameterTypes.size() - 1)
                    sb.append(this.parameterTypes.get(i).prettyPrint()).append(",");
                else sb.append(this.parameterTypes.get(i).prettyPrint());
            }
            return sb.append(")").toString();
        }
    }

    private static class ArrayType implements Type {
        Type elementType;
        int length;

        public ArrayType(Type elementType, int length) {
            this.elementType = elementType;
            this.length = length;
        }
        @Override
        public String prettyPrint() {
            return this.elementType.prettyPrint() + "[" + this.length + "]";
        }
        // Project 4 -- Type Equality
        public boolean equals(Object t) {
            return t instanceof ArrayType && this.elementType.equals(((ArrayType) t).elementType);
        }
    }

    private static class StructureType implements Type {
        String tag;
        LinkedHashMap<String, Type> members;  // for safety, only used for print
        boolean isComplete;

        public StructureType(String tag) {
            this.tag = tag;
            this.members = new LinkedHashMap<>();
            this.isComplete = false;
        }

        public void setComplete(LinkedHashMap<String, Type> members) {
            this.isComplete = true;
            this.members.putAll(members);
        }
        @Override
        public String prettyPrint() {
            return "struct " + this.tag;
        }
        @Override
        public String fullPrint() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.prettyPrint()).append("{");
            for (Map.Entry<String, Type> m : members.entrySet())
                sb.append(m.getValue().prettyPrint()).append(" ").append(m.getKey()).append(";");
            return sb.append("}").toString();
        }
        // Project 4 -- Type Equality
        public boolean equals(Object t) {
            return this == t;
        }
    }

    private static class PointerType implements Type {
        Type referenceType;

        public PointerType(Type referenceType) {
            this.referenceType = referenceType;
        }
        @Override
        public String prettyPrint() {
            return referenceType.prettyPrint() + "*";
        }
        // Project 4 -- Type Equality
        @Override
        public boolean equals(Object t) {
            return t instanceof PointerType && this.referenceType.equals(((PointerType) t).referenceType);
        }
    }

    // Symbol Tableeeeeeeee
    private static class Symbol {
        String name;
        Type type;
        boolean isDefined;

        public Symbol(String name, Type type, boolean isDefined) {
            this.name = name;
            this.type = type;
            this.isDefined = isDefined;
        }
    }

    private static class Scope {
        Scope parent;
        LinkedHashMap<String, Symbol> identifiers;  // 'other' namespace
        LinkedHashMap<String, StructureType> tags;  // tag namespace

        public Scope(Scope parent) {
            this.parent = parent;
            this.identifiers = new LinkedHashMap<>();
            this.tags = new LinkedHashMap<>();
        }

        public void defineId(Symbol s) {
            this.identifiers.put(s.name, s);
        }

        public void defineTag(StructureType st) {
            this.tags.put(st.tag, st);
        }

        public Symbol lookupId(String name) {
            Symbol s = this.identifiers.get(name);
            if (s != null) return s;
            else if (this.parent != null) return this.parent.lookupId(name);
            else return null;
        }

        public Symbol lookupIdThis(String name) {
            return this.identifiers.get(name);
        }

        public StructureType lookupTag(String tag) {
            StructureType s = this.tags.get(tag);
            if (s != null) return s;
            else if (this.parent != null) return this.parent.lookupTag(tag);
            else return null;
        }

        public StructureType lookupTagThis(String tag) {
            return this.tags.get(tag);
        }
    }

    // Main Process, i.e. a SUPERBIG symbol table
    private class myVisitor extends SplcBaseVisitor<Void> {
        Scope fileScope, curScope;
        private Deque<Scope> scopeStack;
        LinkedHashMap<String, Type> variables;
        LinkedHashMap<String, FunctionType> functions;
        LinkedHashMap<String, TerminalNode> incompleteIdentifiers;
        ArrayDeque<FunctionType> functionStack;

        public myVisitor() {
            this.fileScope = new Scope(null);
            this.scopeStack = new ArrayDeque<>();
            this.variables = new LinkedHashMap<>();
            this.functions = new LinkedHashMap<>();
            this.incompleteIdentifiers = new LinkedHashMap<>();
            this.functionStack = new ArrayDeque<>();
            this.enterScope(fileScope);
        }

        private void enterScope(Scope s) {
            this.scopeStack.push(s);
            this.curScope = this.scopeStack.peek();
        }
        private void exitScope() {
            this.scopeStack.pop();
            this.curScope = this.scopeStack.peek();
        }

        // for cases that completed later
        private boolean isComplete(Type t) {
            if (t instanceof ArrayType) return isComplete(((ArrayType) t).elementType);
            else if (t instanceof StructureType) {
                StructureType st = fileScope.lookupTag(((StructureType) t).tag);
                return st != null && st.isComplete;
            }
            else return true;
        }
        private boolean isCompleteThis(Type t) {
            if (t instanceof ArrayType) return isComplete(((ArrayType) t).elementType);
            else if (t instanceof StructureType) return ((StructureType) t).isComplete;
            else return true;
        }

        private TerminalNode getVarDecIdentifier(SplcParser.VarDecContext ctx) {
            if (ctx instanceof SplcParser.VarDecBaseContext)
                return ((SplcParser.VarDecBaseContext) ctx).Identifier();
            else if (ctx instanceof SplcParser.VarDecArrayContext)
                return getVarDecIdentifier(((SplcParser.VarDecArrayContext) ctx).varDec());
            else if (ctx instanceof SplcParser.VarDecPointerContext)
                return getVarDecIdentifier(((SplcParser.VarDecPointerContext) ctx).varDec());
            else if (ctx instanceof SplcParser.VarDecCombContext)
                return getVarDecIdentifier(((SplcParser.VarDecCombContext) ctx).varDec());
            return null;
        }
        private String getVarDecName(TerminalNode id) {
            return id != null ? id.getText() : null;
        }

        private void checkNestedStructureRedeclaration(SplcParser.SpecifierContext ctx, String tag) {
            for (int i = 0; i < ctx.specifier().size(); i++) {
                SplcParser.SpecifierContext mSpec = ctx.specifier(i);
                if (mSpec.STRUCT() != null && mSpec.Identifier() != null && mSpec.LBRACE() != null) {
                    String nestedTag = mSpec.Identifier().getText();
                    if (nestedTag.equals(tag))
                        grader.reportSemanticError(Project3SemanticError.redeclaration(mSpec.Identifier()));
                    checkNestedStructureRedeclaration(mSpec, tag);
                }
            }
        }
        private Type parseBaseType(SplcParser.SpecifierContext ctx) {
            if (ctx.INT() != null) return new PrimitiveType("int");
            else if (ctx.CHAR() != null) return new PrimitiveType("char");
            else if (ctx.STRUCT() != null) {
                String tag = ctx.Identifier().getText();
                StructureType st = curScope.lookupTag(tag);  // lookupTagThis -> lookupTag, hope it works...
                if (st == null) {
                    st = new StructureType(tag);
                    curScope.defineTag(st);
                }

                // complete case
                if (ctx.LBRACE() != null) {
                    if (st.isComplete)
                        grader.reportSemanticError(Project3SemanticError.redeclaration(ctx.Identifier()));

                    // process members
                    LinkedHashMap<String, Type> members = new LinkedHashMap<>();
                    for (int i = 0; i < ctx.specifier().size(); i++) {
                        Type mBaseType = parseBaseType(ctx.specifier(i));
                        if (mBaseType == null) continue;

                        TerminalNode mIdentifier = getVarDecIdentifier(ctx.varDec(i));
                        String mName = getVarDecName(mIdentifier);
                        Type mFullType = parseFullType(ctx.varDec(i), mBaseType);

                        if (!isCompleteThis(mFullType))
                            if (mIdentifier != null)
                                grader.reportSemanticError(Project3SemanticError.memberIncomplete(mIdentifier));
                        if (members.containsKey(mName))
                            if (mIdentifier != null)
                                grader.reportSemanticError(Project3SemanticError.memberDuplicate(mIdentifier));

                        members.put(mName, mFullType);
                    }

                    // Check for nested structure redeclaration
                    checkNestedStructureRedeclaration(ctx, tag);
                    st.setComplete(members);
                }
                return st;
            }
            return null;
        }
        private Type parseFullType(SplcParser.VarDecContext ctx, Type baseType) {
            ArrayList<SplcParser.VarDecContext> varDecNodes = new ArrayList<>();
            SplcParser.VarDecContext cur = ctx;
            while (cur != null) {
                if (cur instanceof SplcParser.VarDecArrayContext) {
                    varDecNodes.add(cur);
                    cur = ((SplcParser.VarDecArrayContext) cur).varDec();
                }
                else if (cur instanceof SplcParser.VarDecPointerContext) {
                    varDecNodes.add(cur);
                    cur = ((SplcParser.VarDecPointerContext) cur).varDec();
                }
                else if (cur instanceof SplcParser.VarDecCombContext)
                    cur = ((SplcParser.VarDecCombContext) cur).varDec();
                else break;
            }

            Type curType = baseType;
            for (SplcParser.VarDecContext n : varDecNodes) {
                if (n instanceof SplcParser.VarDecArrayContext)
                    curType = new ArrayType(curType, Integer.parseInt(((SplcParser.VarDecArrayContext) n).Number().getText()));
                else if (n instanceof SplcParser.VarDecPointerContext)
                    curType = new PointerType(curType);
            }

            return curType;
        }

        private ArrayList<Type> parseFunctionParameters(SplcParser.FuncArgsContext args) {
            ArrayList<Type> argTypes = new ArrayList<>();
            for (int i = 0; i < args.specifier().size(); i++) {
                Type argBaseType = parseBaseType(args.specifier(i));
                if (argBaseType == null) continue;

                TerminalNode argIdentifier = getVarDecIdentifier(args.varDec(i));
                String argName = getVarDecName(argIdentifier);
                if (this.curScope.lookupIdThis(argName) != null)
                    grader.reportSemanticError(Project3SemanticError.redefinition(argIdentifier));

                Type argFullType = parseFullType(args.varDec(i), argBaseType);
                argTypes.add(argFullType);
                this.curScope.defineId(new Symbol(argName, argFullType, true));
            }
            return argTypes;
        }

        @Override
        public Void visitGlobalDef(SplcParser.GlobalDefContext ctx) {
            this.curScope = this.scopeStack.peek();  // fileScope
            Type baseType = parseBaseType(ctx.specifier());
            if (baseType == null) return null;

            // function definition
            if (ctx.LBRACE() != null) {
                String funcName = ctx.Identifier().getText();
                Symbol func = this.curScope.lookupIdThis(funcName);
                if (func != null && func.isDefined)
                    grader.reportSemanticError(Project3SemanticError.redefinition(ctx.Identifier()));
                Scope funcArgScope = new Scope(this.curScope);
                enterScope(funcArgScope);
                ArrayList<Type> funcArgTypes = parseFunctionParameters(ctx.funcArgs());
                exitScope();

                FunctionType thisFunction = new FunctionType(baseType, funcArgTypes);
                this.curScope.defineId(new Symbol(funcName, thisFunction, true));  // fileScope
                this.functions.put(funcName, thisFunction);

                this.functionStack.push(thisFunction);
                Scope funcBodyScope = new Scope(this.curScope);
                enterScope(funcBodyScope);
                for (Map.Entry<String, Symbol> id : funcArgScope.identifiers.entrySet())
                    this.curScope.defineId(id.getValue());
                for (SplcParser.StatementContext stmt : ctx.statement())
                    visit(stmt);
                exitScope();
                this.functionStack.pop();
            }
            // declaration
            else if (ctx.funcArgs() != null) {
                String funcName = ctx.Identifier().getText();
                if (this.curScope.lookupIdThis(funcName) != null)
                    grader.reportSemanticError(Project3SemanticError.redeclaration(ctx.Identifier()));
                Scope funcArgScope = new Scope(this.curScope);
                enterScope(funcArgScope);
                ArrayList<Type> funcArgTypes = parseFunctionParameters(ctx.funcArgs());
                exitScope();

                FunctionType thisFunction = new FunctionType(baseType, funcArgTypes);
                this.curScope.defineId(new Symbol(funcName, thisFunction, false));  // fileScope
                this.functions.put(funcName, thisFunction);
            }
            else if (ctx.varDec() != null) {
                TerminalNode varIdentifier = getVarDecIdentifier(ctx.varDec());
                String varName = getVarDecName(varIdentifier);

                // Check redefinition
                if (this.curScope.lookupIdThis(varName) != null)
                    if (varIdentifier != null)
                        grader.reportSemanticError(Project3SemanticError.redefinition(varIdentifier));

                Type varType = parseFullType(ctx.varDec(), baseType);
                if (!isComplete(varType))
                    if (varIdentifier != null)
                        if (varType instanceof StructureType)
                            this.incompleteIdentifiers.put(varName, varIdentifier);
                        else
                            grader.reportSemanticError(Project3SemanticError.definitionIncomplete(varIdentifier));

                this.curScope.defineId(new Symbol(varName, varType, true));
                this.variables.put(varName, varType);
            }

            return null;
        }

        // Project 4 Start
        private boolean isInteger(Expr e) {
            return e != null && e.type instanceof PrimitiveType && ((PrimitiveType) e.type).name.equals("int");
        }
        private boolean isPointer(Expr e) {
            return e != null && e.type instanceof PointerType;
        }
        private boolean isNullPointer(SplcParser.ExpressionContext ctx) {
            if (ctx instanceof SplcParser.ExprNumContext) {
                var n = ((SplcParser.ExprNumContext) ctx).Number();
                return n != null && "0".equals(n.getText());
            }
            else if (ctx instanceof SplcParser.ExprParenContext) {
                return isNullPointer(((SplcParser.ExprParenContext) ctx).expression());
            }
            else return false;
        }

        // Expression Attributes
        private static class Expr {
            Type type;
            boolean valueCategory;  // 0 -> lvalue, 1 -> rvalue

            public Expr(Type type, boolean valueCategory) {
                this.type = type;
                this.valueCategory = valueCategory;
            }
        }
        // Expression Semantic Check
        private class ExprVisitor extends SplcBaseVisitor<Expr> {
            private Expr parseExpression(SplcParser.ExpressionContext ctx) {
                return switch (ctx) {
                    case SplcParser.ExprIDContext exprIDContext -> visitExprID(exprIDContext);
                    case SplcParser.ExprNumContext exprNumContext -> visitExprNum(exprNumContext);
                    case SplcParser.ExprCharContext exprCharContext -> visitExprChar(exprCharContext);
                    case SplcParser.ExprParenContext exprParenContext -> visitExprParen(exprParenContext);
                    case SplcParser.ExprSuffixContext exprSuffixContext -> visitExprSuffix(exprSuffixContext);
                    case SplcParser.ExprFuncCallContext exprFuncCallContext -> visitExprFuncCall(exprFuncCallContext);
                    case SplcParser.ExprArrayContext exprArrayContext -> visitExprArray(exprArrayContext);
                    case SplcParser.ExprStructureContext exprStructureContext ->
                            visitExprStructure(exprStructureContext);
                    case SplcParser.ExprPrefixContext exprPrefixContext -> visitExprPrefix(exprPrefixContext);
                    case SplcParser.ExprSDMContext exprSDMContext -> visitExprSDM(exprSDMContext);
                    case SplcParser.ExprPMContext exprPMContext -> visitExprPM(exprPMContext);
                    case SplcParser.ExprCompareContext exprCompareContext -> visitExprCompare(exprCompareContext);
                    case SplcParser.ExprEQContext exprEQContext -> visitExprEQ(exprEQContext);
                    case SplcParser.ExprAndContext exprAndContext -> visitExprAnd(exprAndContext);
                    case SplcParser.ExprOrContext exprOrContext -> visitExprOr(exprOrContext);
                    case SplcParser.ExprAssignContext exprAssignContext -> visitExprAssign(exprAssignContext);
                    case null, default -> null;
                };
            }

            @Override
            public Expr visitExprEQ(SplcParser.ExprEQContext ctx) {
                Expr lhs = parseExpression(ctx.expression(0));
                Expr rhs = parseExpression(ctx.expression(1));
                if ((isInteger(lhs) && isInteger(rhs)) ||
                        (isPointer(lhs) && isPointer(rhs) && lhs.type.equals(rhs.type)) ||
                        (isPointer(lhs) && isNullPointer(ctx.expression(1))) ||
                        (isNullPointer(ctx.expression(0)) && isPointer(rhs)))
                    return new Expr(new PrimitiveType("int"), false);
                else {
                    Token token = (ctx.EQ() != null ? ctx.EQ().getSymbol() : (ctx.NEQ() != null ? ctx.NEQ().getSymbol() : null));
                    Project4SemanticError.unmatchedTypeForBinaryOP(ctx, token, lhs.type, rhs.type).throwException();
                    return null;
                }
            }

            @Override
            public Expr visitExprAssign(SplcParser.ExprAssignContext ctx) {
                Expr lhs = parseExpression(ctx.expression(0));
                Expr rhs = parseExpression(ctx.expression(1));
                if (lhs == null || lhs.valueCategory) Project4SemanticError.lvalueRequired(ctx).throwException();
                if ((isInteger(lhs) && isInteger(rhs)) ||
                        (isPointer(lhs) && isPointer(rhs) && lhs.type.equals(rhs.type)))
                    return new Expr(rhs.type, false);
                else if (isPointer(lhs) && isNullPointer(ctx.expression(1)))
                    return new Expr(lhs.type, false);
                else {
                    Token token = (ctx.ASSIGN() != null ? ctx.ASSIGN().getSymbol() : null);
                    Project4SemanticError.unmatchedTypeForBinaryOP(ctx, token, lhs.type, rhs.type).throwException();
                    return null;
                }
            }

            @Override
            public Expr visitExprNum(SplcParser.ExprNumContext ctx) {
                return new Expr(new PrimitiveType("int"), true);
            }

            @Override
            public Expr visitExprCompare(SplcParser.ExprCompareContext ctx) {
                Expr lhs = parseExpression(ctx.expression(0));
                Expr rhs = parseExpression(ctx.expression(1));
                if ((isInteger(lhs) && isInteger(rhs)))
                    return new Expr(new PrimitiveType("int"), true);
                else {
                    Project4SemanticError.unexpectedType(ctx,
                            !isInteger(lhs) ? lhs.type : rhs.type).throwException();
                    return null;
                }
            }

            @Override
            public Expr visitExprFuncCall(SplcParser.ExprFuncCallContext ctx) {
                String name = ctx.Identifier().getText();
                Symbol s = curScope.lookupId(name);
                if (s == null || !(s.type instanceof FunctionType))
                    Project4SemanticError.identifierNotFunction(ctx, name).throwException();
                int requires = ((FunctionType) s.type).parameterTypes.size();
                int given = ctx.expression() != null ? ctx.expression().size() : 0;
                if (requires != given) Project4SemanticError.badParamCount(ctx, requires, given).throwException();
                if (ctx.expression() != null)
                    for (int i = 0; i < ctx.expression().size(); i++) {
                        Expr paramExpr = parseExpression(ctx.expression(i));
                        Type paramType = ((FunctionType) s.type).parameterTypes.get(i);
                        if (paramExpr == null || !paramExpr.type.equals(paramType))
                            Project4SemanticError.badParamType(ctx, i + 1).throwException();
                    }
                return new Expr(((FunctionType) s.type).returnType, true);
            }

            @Override
            public Expr visitExprParen(SplcParser.ExprParenContext ctx) {
                return parseExpression(ctx.expression());
            }

            @Override
            public Expr visitExprOr(SplcParser.ExprOrContext ctx) {
                Expr lhs = parseExpression(ctx.expression(0));
                Expr rhs = parseExpression(ctx.expression(1));
                if (lhs == null || rhs == null) return null;
                if (!isInteger(lhs) && !isPointer(lhs))
                    Project4SemanticError.unexpectedType(ctx, lhs.type).throwException();
                if (!isInteger(rhs) && !isPointer(rhs))
                    Project4SemanticError.unexpectedType(ctx, rhs.type).throwException();
                return new Expr(new PrimitiveType("int"), true);
            }

            @Override
            public Expr visitExprPrefix(SplcParser.ExprPrefixContext ctx) {
                Expr op = parseExpression(ctx.expression());
                if (op == null) return null;
                // get address
                if (ctx.AMP() != null) {
                    if (op.valueCategory) Project4SemanticError.lvalueRequired(ctx).throwException();
                    else return new Expr(new PointerType(op.type), true);
                }
                // unreferencing
                else if (ctx.STAR() != null) {
                    if (!(op.type instanceof PointerType))
                        Project4SemanticError.unexpectedType(ctx, op.type).throwException();
                    else return new Expr(((PointerType) op.type).referenceType, false);
                }
                // self increasing/decreasing
                else if (ctx.INC() != null || ctx.DEC() != null) {
                    if (!isInteger(op) && !isPointer(op))
                        Project4SemanticError.unexpectedType(ctx, op.type).throwException();
                    else if (op.valueCategory)
                        Project4SemanticError.lvalueRequired(ctx).throwException();
                    else return new Expr(op.type, true);
                }
                // unary plus/minus
                else if (ctx.PLUS() != null || ctx.MINUS() != null) {
                    if (!isInteger(op)) Project4SemanticError.unexpectedType(ctx, op.type).throwException();
                    else return new Expr(new PrimitiveType("int"), true);
                }
                // logical not
                else if (ctx.NOT() != null) {
                    if (!isInteger(op) && !isPointer(op))
                        Project4SemanticError.unexpectedType(ctx, op.type).throwException();
                    else return new Expr(new PrimitiveType("int"), true);
                }
                return null;
            }

            @Override
            public Expr visitExprStructure(SplcParser.ExprStructureContext ctx) {
                Expr lhs = parseExpression(ctx.expression());
                if (lhs == null) return null;
                String member = ctx.Identifier().getText();
                // Structure Member Access
                if (ctx.DOT() != null) {
                    if (!(lhs.type instanceof StructureType) || !((StructureType) lhs.type).isComplete)
                        Project4SemanticError.unexpectedType(ctx, lhs.type).throwException();
                    else if (lhs.valueCategory)
                        Project4SemanticError.lvalueRequired(ctx).throwException();
                    else if (((StructureType) lhs.type).members.get(member) == null)
                        Project4SemanticError.badMember(ctx, lhs.type, member).throwException();
                    return new Expr(((StructureType) lhs.type).members.get(member), false);
                }
                // Structure Pointer Access
                else {
                    if (!(lhs.type instanceof PointerType))
                        Project4SemanticError.unexpectedType(ctx, lhs.type).throwException();
                    if (!(((PointerType) lhs.type).referenceType instanceof StructureType) ||
                            !((StructureType) ((PointerType) lhs.type).referenceType).isComplete)
                        Project4SemanticError.unexpectedType(ctx, ((PointerType) lhs.type).referenceType).throwException();
                    else if (((StructureType) ((PointerType) lhs.type).referenceType).members.get(member) == null)
                        Project4SemanticError.badMember(ctx, ((PointerType) lhs.type).referenceType, member).throwException();
                    return new Expr(((StructureType) ((PointerType) lhs.type).referenceType).members.get(member), false);
                }
            }

            @Override
            public Expr visitExprAnd(SplcParser.ExprAndContext ctx) {
                Expr lhs = parseExpression(ctx.expression(0));
                Expr rhs = parseExpression(ctx.expression(1));
                if (lhs == null || rhs == null) return null;
                if (!isInteger(lhs) && !isPointer(lhs))
                    Project4SemanticError.unexpectedType(ctx, lhs.type).throwException();
                if (!isInteger(rhs) && !isPointer(rhs))
                    Project4SemanticError.unexpectedType(ctx, rhs.type).throwException();
                return new Expr(new PrimitiveType("int"), true);
            }

            @Override
            public Expr visitExprID(SplcParser.ExprIDContext ctx) {
                String name = ctx.Identifier().getText();
                Symbol s = curScope.lookupId(name);
                if (s == null || s.type instanceof FunctionType)
                    Project4SemanticError.identifierNotVariable(ctx, name).throwException();
                return new Expr(s.type, false);
            }

            @Override
            public Expr visitExprPM(SplcParser.ExprPMContext ctx) {
                Expr lhs = parseExpression(ctx.expression(0));
                Expr rhs = parseExpression(ctx.expression(1));
                if (lhs == null || rhs == null) return null;
                // integer +/- integer is allowed
                if (isInteger(lhs) && isInteger(rhs))
                    return new Expr(new PrimitiveType("int"), true);
                else if (isInteger(lhs) && isPointer(rhs))
                    // integer + pointer is allowed -> pointer
                    if (ctx.PLUS() != null) return new Expr(rhs.type, true);
                    // integer - pointer is not allowed
                    else Project4SemanticError.unmatchedTypeForBinaryOP(ctx, ctx.MINUS().getSymbol(), lhs.type, rhs.type).throwException();
                // pointer +/- integer is allowed -> pointer
                else if (isPointer(lhs) && isInteger(rhs))
                    return new Expr(lhs.type, true);
                else if (isPointer(lhs) && isPointer(rhs)) {
                    // pointer - pointer is allowed
                    if (ctx.MINUS() != null) {
                        if (lhs.type.equals(rhs.type))
                            return new Expr(new PrimitiveType("int"), true);
                        else {
                            Token token = ctx.MINUS().getSymbol();
                            Project4SemanticError.unmatchedTypeForBinaryOP(ctx, token, lhs.type, rhs.type).throwException();
                        }
                    }
                    // pointer + pointer is not allowed
                    else {
                        Token token = ctx.PLUS().getSymbol();
                        Project4SemanticError.unmatchedTypeForBinaryOP(ctx, token, lhs.type, rhs.type).throwException();
                    }
                }
                Token token = ctx.PLUS() != null ? ctx.PLUS().getSymbol()
                        : (ctx.MINUS() != null ? ctx.MINUS().getSymbol() : null);
                Project4SemanticError.unmatchedTypeForBinaryOP(ctx, token, lhs.type, rhs.type).throwException();
                return null;
            }

            @Override
            public Expr visitExprSuffix(SplcParser.ExprSuffixContext ctx) {
                Expr op = parseExpression(ctx.expression());
                if (op == null) return null;
                if (!isInteger(op) && !isPointer(op))
                    Project4SemanticError.unexpectedType(ctx, op.type).throwException();
                else if (op.valueCategory)
                    Project4SemanticError.lvalueRequired(ctx).throwException();
                return new Expr(op.type, true);
            }

            @Override
            public Expr visitExprArray(SplcParser.ExprArrayContext ctx) {
                Expr lhs = parseExpression(ctx.expression(0));
                Expr rhs = parseExpression(ctx.expression(1));
                if (!(lhs.type instanceof ArrayType || lhs.type instanceof PointerType))
                    Project4SemanticError.unexpectedType(ctx, lhs.type).throwException();
                if (lhs.type instanceof ArrayType && lhs.valueCategory)
                    Project4SemanticError.lvalueRequired(ctx).throwException();
                if (!isInteger(rhs)) Project4SemanticError.unexpectedType(ctx, rhs.type).throwException();

                if (lhs.type instanceof ArrayType)
                    return new Expr(((ArrayType) lhs.type).elementType, false);
                else return new Expr(((PointerType) lhs.type).referenceType, false);
            }

            @Override
            public Expr visitExprSDM(SplcParser.ExprSDMContext ctx) {
                Expr lhs = parseExpression(ctx.expression(0));
                Expr rhs = parseExpression(ctx.expression(1));
                if (isInteger(lhs) && isInteger(rhs))
                    return new Expr(new PrimitiveType("int"), true);
                else if (!isInteger(lhs))
                    Project4SemanticError.unexpectedType(ctx, lhs.type).throwException();
                else
                    Project4SemanticError.unexpectedType(ctx, rhs.type).throwException();
                return null;
            }
        }

        @Override
        public Void visitVarDecStmt(SplcParser.VarDecStmtContext ctx) {
            Type specType = parseBaseType(ctx.specifier());
            if (specType == null) return null;

            TerminalNode varIdentifier = getVarDecIdentifier(ctx.varDec());
            String varName = getVarDecName(varIdentifier);

            // Check redefinition
            if (this.curScope.lookupIdThis(varName) != null)
                if (varIdentifier != null)
                    grader.reportSemanticError(Project3SemanticError.redefinition(varIdentifier));

            Type varType = parseFullType(ctx.varDec(), specType);
            if (!isCompleteThis(varType))
                if (varIdentifier != null)
                    grader.reportSemanticError(Project3SemanticError.definitionIncomplete(varIdentifier));

            this.curScope.defineId(new Symbol(varName, varType, true));
            // Project 4 -- local varDec semantics
            if (ctx.ASSIGN() != null) {
                try {
                    Expr expr = new ExprVisitor().visit(ctx.expression());
                    if (!(varType instanceof PrimitiveType && ((PrimitiveType) varType).name.equals("int") && varType.equals(expr.type))
                        && !(varType instanceof PointerType && (varType.equals(expr.type) || isNullPointer(ctx.expression())))) {
                        Token token = ctx.ASSIGN().getSymbol();
                        Project4SemanticError.unmatchedTypeForBinaryOP(ctx.expression(), token, varType, expr.type).throwException();
                    }
                }
                catch (Project4Exception e) {
                    grader.reportSemanticError(e);
                }
            }
            return null;
        }

        // Project 4 -- If statement semantics
        @Override
        public Void visitIfStmt(SplcParser.IfStmtContext ctx) {
            if (ctx.expression() != null) {
                try {
                    Expr expr = new ExprVisitor().visit(ctx.expression());
                    if (!isInteger(expr) && !isPointer(expr))
                        Project4SemanticError.unexpectedType(ctx.expression(), expr.type).throwException();
                } catch (Project4Exception e) {
                    grader.reportSemanticError(e);
                }
            }
            visit(ctx.statement(0));
            if (ctx.ELSE() != null) visit(ctx.statement(1));
            return null;
        }

        // Project 4 -- While statement semantics
        @Override
        public Void visitWhileStmt(SplcParser.WhileStmtContext ctx) {
            if (ctx.expression() != null) {
                try {
                    Expr expr = new ExprVisitor().visit(ctx.expression());
                    if (!isInteger(expr) && !isPointer(expr))
                        Project4SemanticError.unexpectedType(ctx.expression(), expr.type).throwException();
                } catch (Project4Exception e) {
                    grader.reportSemanticError(e);
                }
            }
            visit(ctx.statement());
            return null;
        }

        // Project 4 -- Return statement semantics
        @Override
        public Void visitReturnStmt(SplcParser.ReturnStmtContext ctx) {
            Type expected = this.functionStack.isEmpty() ? null : this.functionStack.peek().returnType;
            if (ctx.expression() != null) {
                try {
                    Expr expr = new ExprVisitor().visit(ctx.expression());
                    if (expected != null) {
                        if (!expected.equals(expr.type)) Project4SemanticError.unexpectedType(ctx.expression(), expr.type).throwException();
                    }
                } catch (Project4Exception e) {
                    grader.reportSemanticError(e);
                }
            }
            else {
                if (expected != null) {
                    try {
                        Project4SemanticError.unexpectedType(ctx.expression(), expected).throwException();
                    } catch (Project4Exception e) {
                        grader.reportSemanticError(e);
                    }
                }
            }
            return null;
        }

        @Override
        public Void visitExprStmt(SplcParser.ExprStmtContext ctx) {
            try {
                new ExprVisitor().visit(ctx.expression());
            } catch (Project4Exception e) {
                grader.reportSemanticError(e);
            }
            return null;
        }

        @Override
        public Void visitBlockStmt(SplcParser.BlockStmtContext ctx) {
            enterScope(new Scope(this.curScope));
            for (SplcParser.StatementContext stmt : ctx.statement()) visit(stmt);
            exitScope();
            return null;
        }

        @Override
        public Void visitExprID(SplcParser.ExprIDContext ctx) {
            String name = ctx.Identifier().getText();
            Symbol s = this.curScope.lookupId(name);
            if (s == null)
                grader.reportSemanticError(Project3SemanticError.undeclaredUse(ctx.Identifier()));
            return null;
        }

        @Override
        public Void visitExprFuncCall(SplcParser.ExprFuncCallContext ctx) {
            String name = ctx.Identifier().getText();
            Symbol s = this.curScope.lookupId(name);
            if (s == null)
                grader.reportSemanticError(Project3SemanticError.undeclaredUse(ctx.Identifier()));
            if (ctx.expression() != null)
                for (SplcParser.ExpressionContext expr : ctx.expression()) visit(expr);
            return null;
        }
    }
}
