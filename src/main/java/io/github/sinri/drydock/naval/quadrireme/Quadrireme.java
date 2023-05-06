package io.github.sinri.drydock.naval.quadrireme;

import io.github.sinri.drydock.naval.galley.Galley;

/**
 * 四段帆船。
 * 基本上就是 Galley。
 */
@Deprecated
abstract public class Quadrireme extends Galley {
    @Override
    final protected void launchAsGalley() {
        this.launchAsQuadrireme();
    }

    abstract protected void launchAsQuadrireme();
}
