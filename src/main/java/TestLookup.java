import java.util.HashMap;
import java.util.Map;

public class TestLookup {
    public static void main(String[] args) {
        // 创建全局作用域
        Scope global = new Scope(null);
        global.define("x", 100);
        global.define("y", 200);

        // 创建函数作用域（子作用域）
        Scope func = new Scope(global);
        func.define("x", 10); // 遮蔽全局的 x
        func.define("z", 30);

        // 创建块作用域（孙作用域）
        Scope block = new Scope(func);
        block.define("w", 40);

        // 测试查找
        System.out.println("block.lookup('w'): " + block.lookup("w")); // w=40
        System.out.println("block.lookup('z'): " + block.lookup("z")); // z=30
        System.out.println("block.lookup('x'): " + block.lookup("x")); // x=10 (被遮蔽)
        System.out.println("block.lookup('y'): " + block.lookup("y")); // y=200 (从全局继承)
        System.out.println("block.lookup('notExist'): " + block.lookup("notExist")); // null
    }

    // 表示一个整型变量
    private static class IntVariable {
        String name;
        int value;

        public IntVariable(String name, int value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }
    }

    // 表示一个作用域
    private static class Scope {
        Map<String, IntVariable> variables = new HashMap<>();
        Scope parent; // 指向父作用域，根作用域的 parent 为 null

        public Scope(Scope parent) {
            this.parent = parent;
        }

        // 在当前作用域中定义一个变量
        // 如果已存在同名变量，会覆盖（即在一个定义域内可以重复定义变量）
        public void define(String name, int value) {
            // TODO:定义变量
            variables.put(name, new IntVariable(name, value));
        }

        // TODO: 在当前作用域或其父作用域中查找变量
        // 请补全此方法
        // 如果找到，返回对应的 IntVariable
        // 如果未找到，返回 null
        public IntVariable lookup(String name) {
            // 请在此处补全代码
            // 提示：先查当前作用域，查不到则递归查父作用域
            return variables.getOrDefault(name, parent != null ? parent.lookup(name) : null);
        }
    }
}

