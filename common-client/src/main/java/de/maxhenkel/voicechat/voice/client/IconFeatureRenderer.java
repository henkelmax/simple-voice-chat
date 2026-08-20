package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.List;

/**
 * Renders icons in the see-through phase, where vanilla renders see-through name tags.
 * That phase runs after the OIT pass, so see-through render types, which have no OIT pipelines, can be used there.
 */
public class IconFeatureRenderer extends RenderTypeFeatureRenderer<IconFeatureRenderer.Submit> {

    public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("Voice Chat Icon");

    @Override
    protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
        for (Submit submit : submits) {
            submit.geometry().render(submit.pose(), getVertexBuilder(submit.renderType()));
        }
    }

    public record Submit(PoseStack.Pose pose, RenderType renderType,
                         SubmitNodeCollector.CustomGeometryRenderer geometry) implements TranslucentSubmit {

        @Override
        public float distanceToCameraSq() {
            return TranslucentSubmit.computeDistanceToCameraSq(pose.pose());
        }

        @Override
        public FeatureRendererType<Submit> featureType() {
            return TYPE;
        }
    }

}
