package com.simibubi.create.foundation.block.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.simibubi.create.Create;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("deprecation")
public abstract class CustomRenderedItemModel extends WrappedBakedModel {

	protected String basePath;
	protected Map<String, IBakedModel> partials = new HashMap<>();
	protected DynamicItemRenderer renderer;

	public CustomRenderedItemModel(IBakedModel template, String basePath) {
		super(template);
		this.basePath = basePath;
		this.renderer = createRenderer();
	}

	public final List<ResourceLocation> getModelLocations() {
		return partials.keySet().stream().map(this::getPartialModelLocation).collect(Collectors.toList());
	}
	
	public DynamicItemRenderer getRenderer() {
		return renderer;
	}

	public abstract DynamicItemRenderer createRenderer();

	@Override
	public boolean isBuiltInRenderer() {
		return true;
	}

	protected void addPartials(String... partials) {
		this.partials.clear();
		for (String name : partials)
			this.partials.put(name, null);
	}

	public CustomRenderedItemModel loadPartials(ModelBakery bakery) {
		for (String name : partials.keySet())
			partials.put(name, loadModel(bakery, name));
		return this;
	}

	private IBakedModel loadModel(ModelBakery bakery, String name) {
		return bakery.func_217845_a(getPartialModelLocation(name), ModelRotation.X0_Y0);
	}

	private ResourceLocation getPartialModelLocation(String name) {
		return new ResourceLocation(Create.ID, "item/" + basePath + "/" + name);
	}

	public IBakedModel getPartial(String name) {
		return partials.get(name);
	}

}
