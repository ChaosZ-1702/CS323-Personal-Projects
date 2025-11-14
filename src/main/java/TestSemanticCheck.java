public class TestSemanticCheck {
    public static void main(String[] args) {
        Scope global = new Scope(null);

        // 正常定义
        global.define(new VariableSymbol("x", new IntType()));
        System.out.println("Defined: x: int");

        global.define(new VariableSymbol("y", new StringType()));
        System.out.println("Defined: y: string");

        // 尝试重复定义 x
        try {
            global.define(new VariableSymbol("x", new StringType()));
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // 创建子作用域
        Scope local = new Scope(global);

        // 子作用域可以定义 x（因为是不同作用域）
        local.define(new VariableSymbol("x", new IntType()));
        System.out.println("Defined in local: x: int");

        // 在子作用域内不能重复定义 x
        try {
            local.define(new VariableSymbol("x", new StringType()));
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 表示变量类型
    interface Type {}
    private static class IntType implements Type { public String toString() { return "int"; } }
    private static class StringType implements Type { public String toString() { return "string"; } }

    // 表示一个变量符号
    private static class VariableSymbol {
        String name;
        Type type;

        public VariableSymbol(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String toString() {
            return name + ": " + type;
        }
    }

    // 表示一个作用域
    private static class Scope {
        java.util.Map<String, VariableSymbol> symbols = new java.util.HashMap<>();
        Scope parent; // 父作用域

        public Scope(Scope parent) {
            this.parent = parent;
        }

        // TODO: 在当前作用域中定义一个变量
        // 要求：
        // 1. 如果当前作用域已存在同名变量，抛出 IllegalArgumentException
        // 2. 否则，将变量加入当前作用域的符号表
        public void define(VariableSymbol var) {
            // 请在此处补全代码，实现重复定义检查
            String varName = var.name;
            if (symbols.containsKey(varName)) throw new IllegalArgumentException("Variable name " + varName + " already exists in this Scope.");
            else symbols.put(varName, var);
        }

        // 提供一个简单的查找方法（无需修改）
        public VariableSymbol lookup(String name) {
            VariableSymbol sym = symbols.get(name);
            if (sym != null) return sym;
            if (parent != null) return parent.lookup(name);
            return null;
        }
    }
}