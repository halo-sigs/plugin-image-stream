package run.halo.imagestream;

import org.springframework.stereotype.Component;

import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

/**
 * @author ryanwang
 * @since 2.0.0
 */
@Component
public class ImageStreamPlugin extends BasePlugin {

    public ImageStreamPlugin(PluginContext context) {
        super(context);
    }

}
