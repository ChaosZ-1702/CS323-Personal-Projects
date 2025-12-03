package impl.project2;

import generated.Splc.SplcBaseVisitor;
import generated.Splc.SplcParser;

public class ConstExprVisitor extends SplcBaseVisitor<Integer> {
	@Override
	public Integer visitExprNum(SplcParser.ExprNumContext ctx) {
		try {
			return Integer.valueOf(ctx.Number().getText());
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Integer visitExprParen(SplcParser.ExprParenContext ctx) {
		return visit(ctx.expression());
	}

	@Override
	public Integer visitExprPrefix(SplcParser.ExprPrefixContext ctx) {
		if (ctx.PLUS() != null) return visit(ctx.expression());
		else if (ctx.MINUS() != null) return -visit(ctx.expression());
		else return null;
	}

	@Override
	public Integer visitExprPM(SplcParser.ExprPMContext ctx) {
		Integer left = visit(ctx.expression(0));
		Integer right = visit(ctx.expression(1));
		if (left == null || right == null) return null;
		String op = ctx.getChild(1).getText();
        return switch (op) {
            case "+" -> left + right;
            case "-" -> left - right;
            default -> null;
        };
    }

	@Override
	public Integer visitExprSDM(SplcParser.ExprSDMContext ctx) {
		Integer left = visit(ctx.expression(0));
		Integer right = visit(ctx.expression(1));
		if (left == null || right == null) return null;
		String op = ctx.getChild(1).getText();
        return switch (op) {
            case "*" -> left * right;
            case "/" -> left / right;
            case "%" -> left % right;
            default -> null;
        };
    }

}
