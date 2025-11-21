package impl;

import framework.AbstractCompiler;
import framework.AbstractGrader;
import framework.lang.Type;
import framework.project3.Project3SemanticError;
import generated.Splc.SplcBaseVisitor;
import generated.Splc.SplcLexer;
import generated.Splc.SplcParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
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

        for (Map.Entry<String, Type> vs : v.variables.entrySet())
            if (vs.getValue() instanceof StructureType st) {
                if (!st.isComplete && v.incompleteIdentifiers.containsKey(vs.getKey()))
                    grader.reportSemanticError(Project3SemanticError.definitionIncomplete(v.incompleteIdentifiers.get(vs.getKey())));
            }
        grader.print("Variables:\n");
        for (Map.Entry<String, Type> vs : v.variables.entrySet())
            grader.print(vs.getKey() + ": " + vs.getValue().fullPrint() + "\n");

        grader.print("\n");

        grader.print("Functions:\n");
        for (Map.Entry<String, FunctionType> vs : v.functions.entrySet())
            grader.print(vs.getKey() + ": " + vs.getValue().prettyPrint() + "\n");
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

        public myVisitor() {
            this.fileScope = new Scope(null);
            this.scopeStack = new ArrayDeque<>();
            this.variables = new LinkedHashMap<>();
            this.functions = new LinkedHashMap<>();
            this.incompleteIdentifiers = new LinkedHashMap<>();
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

                Scope funcBodyScope = new Scope(this.curScope);
                enterScope(funcBodyScope);
                for (Map.Entry<String, Symbol> id : funcArgScope.identifiers.entrySet())
                    this.curScope.defineId(id.getValue());
                for (SplcParser.StatementContext stmt : ctx.statement())
                    visit(stmt);
                exitScope();
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
