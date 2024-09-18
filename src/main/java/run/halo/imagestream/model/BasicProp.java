package run.halo.imagestream.model;

import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import run.halo.app.infra.utils.JsonUtils;

@Data
public class BasicProp {
    public static final String GROUP = "basic";

    private UnsplashProp unsplash = new UnsplashProp();
    private PixabayProp pixabay = new PixabayProp();
    private PexelsProp pexels = new PexelsProp();

    public void setPexels(PexelsProp pexels) {
        this.pexels = (pexels == null ? new PexelsProp() : pexels);
    }

    public void setPixabay(PixabayProp pixabay) {
        this.pixabay = (pixabay == null ? new PixabayProp() : pixabay);
    }

    public void setUnsplash(UnsplashProp unsplash) {
        this.unsplash = (unsplash == null ? new UnsplashProp() : unsplash);
    }

    public static BasicProp from(Map<String, JsonNode> config) {
        var basic = config.get(GROUP);
        if (basic == null) {
            return new BasicProp();
        }
        return JsonUtils.mapper().convertValue(basic, BasicProp.class);
    }
}
