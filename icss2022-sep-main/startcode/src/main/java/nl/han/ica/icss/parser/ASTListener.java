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
		ast.root.addChild(stylerule);
		currentContainer.push(stylerule);
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		currentContainer.pop();
	}

	@Override
	public void enterSelector(ICSSParser.SelectorContext ctx) {
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
		Declaration declaration = new Declaration(ctx.property().getText());
		currentContainer.peek().addChild(declaration);
		currentContainer.push(declaration);
	}
	 public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		currentContainer.pop();
	 }

}