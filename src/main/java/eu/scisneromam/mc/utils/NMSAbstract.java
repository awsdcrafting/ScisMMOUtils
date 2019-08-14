package eu.scisneromam.mc.utils;

import com.google.common.base.Preconditions;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * The default fallback implementation of {@link NMSAbstract} using reflection
 * to prevent the need to update, assuming no changes to NMS obfuscation were made.
 * This implementation is a last resort if all else fails and there is no alternative
 * implementation provided by VeinMiner
 *
 * Copied from: https://github.com/2008Choco/VeinMiner/blob/master/src/me/choco/veinminer/utils/versions/NMSAbstractDefault.java#L3
 * (2019-08-12) probably now here: https://github.com/2008Choco/VeinMiner/blob/master/src/main/java/wtf/choco/veinminer/utils/ReflectionUtil.java
 *
 */
public class NMSAbstract {

	private boolean wasSuccessful = true;
	private final String version;

	private static Class<?> nmsPlayer;
	private static Class<?> playerInteractManager;
	private static Field fieldPlayerInteractManager;
	private static Class<?> blockPosition;
	private static Constructor<?> constructorBlockPosition;
	private static Class<?> craftPlayer;
	private static Method methodGetHandle;
	private static Method methodBreakBlock;

	public NMSAbstract(String version) {
		this.version = version + ".";
		this.loadNMSClasses();
	}

	public void breakBlock(Player player, Block block) {
		Preconditions.checkArgument(player != null, "A null player is incapable of breaking blocks");
		Preconditions.checkArgument(block != null, "Cannot break a null block");

		if (!wasSuccessful) {
			block.breakNaturally(player.getInventory().getItemInMainHand());
			return;
		}

		try {
			Object cPlayer = methodGetHandle.invoke(player);
			Object interactManager = fieldPlayerInteractManager.get(cPlayer);
			Object blockPos = constructorBlockPosition.newInstance(block.getX(), block.getY(), block.getZ());
			methodBreakBlock.invoke(interactManager, blockPos);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {}
	}

	private final void loadNMSClasses() {
		if (nmsPlayer != null) return;

		nmsPlayer = this.getNMSClass("EntityPlayer");
		playerInteractManager = this.getNMSClass("PlayerInteractManager");
		fieldPlayerInteractManager = this.getField(nmsPlayer, "playerInteractManager");
		blockPosition = this.getNMSClass("BlockPosition");
		constructorBlockPosition = this.getConstructor(blockPosition, Integer.TYPE, Integer.TYPE, Integer.TYPE);
		craftPlayer = this.getCBClass("entity.CraftPlayer");
		methodGetHandle = this.getMethod("getHandle", craftPlayer);
		methodBreakBlock = this.getMethod("breakBlock", playerInteractManager, blockPosition);
	}

	private Method getMethod(String name, Class<?> clazz, Class<?>... paramTypes) {
		try {
			return clazz.getMethod(name, paramTypes);
		} catch (NoSuchMethodException | SecurityException e) {
			System.out.println("Could not find method " + name + " in " + clazz.getSimpleName());
			this.wasSuccessful = false;
		}
		return null;
	}

	private Field getField(Class<?> clazz, String name) {
		try {
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (Exception e) {
			System.out.println("Could not find field " + name + " in " + clazz.getSimpleName());
			this.wasSuccessful = false;
		}
		return null;
	}

	private Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameters) {
		try {
			return clazz.getConstructor(parameters);
		} catch (NoSuchMethodException | SecurityException e) {
			System.out.println("Could not find constructor for class " + clazz.getSimpleName());
			this.wasSuccessful = false;
		}
		return null;
	}

	private Class<?> getNMSClass(String className) {
		try {
			return Class.forName("net.minecraft.server." + version + className);
		} catch (Exception e) {
			System.out.println("Could not find class " + className);
			this.wasSuccessful = false;
		}
		return null;
	}

	private Class<?> getCBClass(String className) {
		try {
			return Class.forName("org.bukkit.craftbukkit." + version + className);
		} catch (Exception e) {
			System.out.println("Could not find class " + className);
			this.wasSuccessful = false;
		}
		return null;
	}
}
