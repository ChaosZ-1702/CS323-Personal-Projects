package impl.project2;

import generated.Splc.SplcBaseVisitor;
import generated.Splc.SplcParser;
import org.antlr.v4.runtime.tree.ParseTree;

public class ConstExprVisitor extends SplcBaseVisitor<Integer> {
	@Override
	public Integer visitExpression(SplcParser.ExpressionContext ctx) {
		// 整数常量
		if (ctx.Number() != null) {
			try {
				return Integer.valueOf(ctx.Number().getText());
			} catch (NumberFormatException e) {
				return null;
			}
		}

		// 检查括号内是否是常量表达式
		if (ctx.getChildCount() == 3 && "(".equals(ctx.getChild(0).getText()) && ")".equals(ctx.getChild(2).getText())) {
			return visit(ctx.expression(0));
		}

		// 一元正号和负号
		if (ctx.getChildCount() == 2 && ctx.expression().size() == 1) {
			String left = ctx.getChild(0).getText();
			if ("+".equals(left) || "-".equals(left)) {
				Integer value = visit(ctx.expression(0));
				if (value == null) return null;
				return "+".equals(left) ? value : -value;
			}
		}

		// 二元运算
		if (ctx.expression().size() == 2) {
			Integer left = visit(ctx.expression(0));
			Integer right = visit(ctx.expression(1));
			if (left == null || right == null) return null;
			String op = ctx.getChild(1).getText();
			switch (op) {
				case "*": return left * right;
				case "/": return (right == 0) ? null : left / right;
				case "%": return (right == 0) ? null : left % right;
				case "+": return left + right;
				case "-": return left - right;
				default:
					// Only arithmetic binary ops are constexpr in this project
					return null;
			}
		}
		return null;
	}

}
