package nl.han.ica.icss.parser;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Stack;


import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	
	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	private final Map<ParserRuleContext, Expression> expressions = new IdentityHashMap<>();

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}
    public AST getAST() {
        return ast;
    }

	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		Stylerule stylerule = new Stylerule();
		// Voeg de stijlregel toe en zet deze op de stack.
		ast.root.addChild(stylerule);
		currentContainer.push(stylerule);
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		// Verwijder de stijlregel van de stack.
		currentContainer.pop();
	}

	@Override
	public void enterSelector(ICSSParser.SelectorContext ctx) {
		// Bepaal welk type selector ANTLR heeft herkend
		if (ctx.LOWER_IDENT() != null) {
			TagSelector selector = new TagSelector(ctx.LOWER_IDENT().getText());
			currentContainer.peek().addChild(selector);

		} else if (ctx.ID_IDENT() != null) {
			IdSelector selector = new IdSelector(ctx.ID_IDENT().getText());
			currentContainer.peek().addChild(selector);

		} else if (ctx.CLASS_IDENT() != null) {
			ClassSelector selector = new ClassSelector(ctx.CLASS_IDENT().getText());
			currentContainer.peek().addChild(selector);
		}
	}

	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		// Maak een declaratie aan met de herkende property.
		Declaration declaration = new Declaration(ctx.property().getText());

		// Voeg de declaratie toe aan de huidige stijlregel.
		currentContainer.peek().addChild(declaration);

		// Zet de declaratie op de stack.
		currentContainer.push(declaration);
	}
	@Override
	 public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		// Voeg de volledige berekening toe aan de declaratie.
		currentContainer.peek().addChild(expressions.get(ctx.expression()));

		// Verlaat de huidige declaratie.
		currentContainer.pop();
	 }

	 @Override
	public void enterLiteral(ICSSParser.LiteralContext ctx) {
		// Bepaal welk type literal ANTLR heeft herkend.
		 if (ctx.PIXELSIZE() != null) {
			 PixelLiteral literal = new PixelLiteral(ctx.PIXELSIZE().getText());
			 expressions.put(ctx, literal);
		 }
		 else if(ctx.PERCENTAGE() != null) {
			 PercentageLiteral literal = new PercentageLiteral(ctx.PERCENTAGE().getText());
			 expressions.put(ctx, literal);
		 }
		 else if(ctx.COLOR() != null) {
			 ColorLiteral literal = new ColorLiteral(ctx.COLOR().getText());
			 expressions.put(ctx, literal);
		 }
		 else if(ctx.TRUE() != null) {
			 BoolLiteral literal = new BoolLiteral(ctx.TRUE().getText());
			 expressions.put(ctx, literal);
		 }
		 else if(ctx.FALSE() != null) {
			 BoolLiteral literal = new BoolLiteral(ctx.FALSE().getText());
			 expressions.put(ctx, literal);
		 }
		 else if (ctx.SCALAR() != null) {
			 ScalarLiteral literal = new ScalarLiteral(ctx.SCALAR().getText());
			 expressions.put(ctx, literal);
		 }
	 }

	@Override
	public void enterAssignment(ICSSParser.AssignmentContext ctx) {
		VariableAssignment assignment = new VariableAssignment();
		VariableReference name = new VariableReference(ctx.CAPITAL_IDENT().getText());

		// Voeg de variabelenaam toe aan de assignment.
		assignment.addChild(name);

		ast.root.addChild(assignment);
		currentContainer.push(assignment);
	}

	@Override
	public void exitAssignment(ICSSParser.AssignmentContext ctx) {
		// Voeg de volledige berekening toe aan de assignment.
		currentContainer.peek().addChild(expressions.get(ctx.expression()));

		// Verlaat de assignment.
		currentContainer.pop();
	}

	@Override
	public void exitTerm(ICSSParser.TermContext ctx) {
		Expression result;

		if (ctx.getChild(0) instanceof ICSSParser.LiteralContext) {
			// Haal de eerste literal op uit de Map.
			result = expressions.get(ctx.literal(0));
		} else {
			// Maak een referentie als de term begint met een variabele.
			result = new VariableReference(ctx.getChild(0).getText());
		}

		// Verwerk alle vermenigvuldigingen.
		for (int i = 0; i < ctx.MUL().size(); i++) {
			ParseTree child = ctx.getChild(2 * i + 2);
			Expression right;

			if (child instanceof ICSSParser.LiteralContext) {
				right = expressions.get(child);
			} else {
				right = new VariableReference(child.getText());
			}

			// Maak een vermenigvuldiging met de linker- en rechterwaarde.
			MultiplyOperation operation = new MultiplyOperation();
			operation.addChild(result);
			operation.addChild(right);

			// Bewaar de nieuwe operation als tussenresultaat.
			result = operation;
		}

		// Bewaar het volledige resultaat van de term.
		expressions.put(ctx, result);
	}
	@Override
	public void exitExpression(ICSSParser.ExpressionContext ctx) {
		// Haal het resultaat van de eerste term op.
		Expression result = expressions.get(ctx.term(0));

		// Verwerk alle optellingen en aftrekkingen.
		for (int i = 1; i < ctx.term().size(); i++) {
			Expression right = expressions.get(ctx.term(i));
			String operator = ctx.getChild(2 * i - 1).getText();

			if (operator.equals("+")) {
				AddOperation operation = new AddOperation();
				operation.addChild(result);
				operation.addChild(right);
				result = operation;
			} else if (operator.equals("-")) {
				SubtractOperation operation = new SubtractOperation();
				operation.addChild(result);
				operation.addChild(right);
				result = operation;
			}
		}

		// Bewaar het volledige resultaat van de expression.
		expressions.put(ctx, result);
	}
}