import generated.Calc1.Calc1Lexer;
import generated.Calc1.Calc1Parser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) {
        // 把要解析的字符串喂给 ANTLR
        CharStream input = CharStreams.fromString("(1 + 2 * 2 / 3)");
        // 词法分析器
        Calc1Lexer lexer = new Calc1Lexer(input);
        // Token 流
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        // 语法分析器
        Calc1Parser parser = new Calc1Parser(tokens);
        // 从规则 'root' 开始解析
        ParseTree tree = parser.root();
        // 把树打印出来看看
        System.out.println(tree.toStringTree(parser));
    }
}