package dev.latvian.kubejs.item.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.kubejs.item.BoundItemStackJS;
import dev.latvian.kubejs.item.EmptyItemStackJS;
import dev.latvian.kubejs.item.ItemStackJS;
import dev.latvian.kubejs.util.UtilsJS;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author LatvianModder
 */
public class TagIngredientJS implements IngredientJS
{
	private static final Map<String, TagIngredientJS> tagIngredientCache = new HashMap<>();

	public static TagIngredientJS createTag(String tag)
	{
		return tagIngredientCache.computeIfAbsent(tag, TagIngredientJS::new);
	}

	public static void clearTagCache()
	{
		tagIngredientCache.clear();
	}

	private final ResourceLocation tag;
	private ITag<Item> actualTag;

	private TagIngredientJS(String t)
	{
		tag = UtilsJS.getMCID(t);
	}

	public String getTag()
	{
		return tag.toString();
	}

	public ITag<Item> getActualTag()
	{
		if (actualTag == null)
		{
			actualTag = TagCollectionManager.getManager().getItemTags().get(tag);

			if (actualTag == null)
			{
				actualTag = Tag.getEmptyTag();
			}
		}

		return actualTag;
	}

	@Override
	public boolean test(ItemStackJS stack)
	{
		return !stack.isEmpty() && getActualTag().contains(stack.getItem());
	}

	@Override
	public boolean testVanilla(ItemStack stack)
	{
		return !stack.isEmpty() && getActualTag().contains(stack.getItem());
	}

	@Override
	public Set<ItemStackJS> getStacks()
	{
		ITag<Item> t = getActualTag();

		if (t.getAllElements().size() > 0)
		{
			NonNullList<ItemStack> list = NonNullList.create();

			for (Item item : t.getAllElements())
			{
				item.fillItemGroup(ItemGroup.SEARCH, list);
			}

			Set<ItemStackJS> set = new LinkedHashSet<>();

			for (ItemStack stack1 : list)
			{
				if (!stack1.isEmpty())
				{
					set.add(new BoundItemStackJS(stack1));
				}
			}

			return set;
		}

		return Collections.emptySet();
	}

	@Override
	public ItemStackJS getFirst()
	{
		ITag<Item> t = getActualTag();

		if (t.getAllElements().size() > 0)
		{
			NonNullList<ItemStack> list = NonNullList.create();

			for (Item item : t.getAllElements())
			{
				item.fillItemGroup(ItemGroup.SEARCH, list);

				for (ItemStack stack : list)
				{
					if (!stack.isEmpty())
					{
						return new BoundItemStackJS(stack);
					}
				}

				list.clear();
			}
		}

		return EmptyItemStackJS.INSTANCE;
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return "'#" + tag + "'";
	}

	@Override
	public JsonElement toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty("tag", tag.toString());
		return json;
	}

	@Override
	public boolean anyStackMatches(IngredientJS ingredient)
	{
		if (ingredient instanceof TagIngredientJS && tag.equals(((TagIngredientJS) ingredient).tag))
		{
			return true;
		}

		return IngredientJS.super.anyStackMatches(ingredient);
	}
}