// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.EngineException;

public final strictfp class _importdrawing
    extends org.nlogo.nvm.Command {
  @Override
  public Syntax syntax() {
    return Syntax.commandSyntax
        (new int[]{Syntax.StringType()},
            "O---", true);
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    try {
      workspace.importDrawing
          (workspace.fileManager().attachPrefix
              (argEvalString(context, 0)));
    } catch (java.io.IOException ex) {
      throw new EngineException
          (context, _importdrawing.this,
              token().name() +
                  ": " + ex.getMessage());
    } catch (RuntimeException ex) {
      throw new EngineException
          (context, _importdrawing.this,
              token().name() +
                  ": " + ex.getMessage());
    }
    context.ip = next;
  }
}
