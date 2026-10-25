package nl.han.ica.icss.parser;

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

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	
	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

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
		// Zet de declaratie op de stack om straks de waarde toe te voegen.
		currentContainer.push(declaration);
	}
	@Override
	 public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		 // Ga terug naar de stijlregel na het verwerken van de declaratie.
		currentContainer.pop();
	 }

	 @Override
	public void enterLiteral(ICSSParser.LiteralContext ctx) {
		// Bepaal welk type literal ANTLR heeft herkend.
		 if (ctx.PIXELSIZE() != null) {
			 PixelLiteral literal = new PixelLiteral(ctx.PIXELSIZE().getText());
			 currentContainer.peek().addChild(literal);
		 }
		 else if(ctx.PERCENTAGE() != null) {
			 PercentageLiteral literal = new PercentageLiteral(ctx.PERCENTAGE().getText());
			 currentContainer.peek().addChild(literal);
		 }
		 else if(ctx.COLOR() != null) {
			 ColorLiteral literal = new ColorLiteral(ctx.COLOR().getText());
			 currentContainer.peek().addChild(literal);
		 }
	 }

}