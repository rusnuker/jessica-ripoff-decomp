/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package net.minecraft.world.storage;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SaveFormatOld
implements ISaveFormat {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final File savesDirectory;
    protected final DataFixer dataFixer;

    public SaveFormatOld(File savesDirectoryIn, DataFixer dataFixerIn) {
        this.dataFixer = dataFixerIn;
        if (!savesDirectoryIn.exists()) {
            savesDirectoryIn.mkdirs();
        }
        this.savesDirectory = savesDirectoryIn;
    }

    @Override
    public String getName() {
        return "Old Format";
    }

    @Override
    public List<WorldSummary> getSaveList() throws AnvilConverterException {
        ArrayList list = Lists.newArrayList();
        int i = 0;
        while (i < 5) {
            String s = "World" + (i + 1);
            WorldInfo worldinfo = this.getWorldInfo(s);
            if (worldinfo != null) {
                list.add(new WorldSummary(worldinfo, s, "", worldinfo.getSizeOnDisk(), false));
            }
            ++i;
        }
        return list;
    }

    @Override
    public void flushCache() {
    }

    @Override
    @Nullable
    public WorldInfo getWorldInfo(String saveName) {
        WorldInfo worldinfo;
        File file1 = new File(this.savesDirectory, saveName);
        if (!file1.exists()) {
            return null;
        }
        File file2 = new File(file1, "level.dat");
        if (file2.exists() && (worldinfo = SaveFormatOld.getWorldData(file2, this.dataFixer)) != null) {
            return worldinfo;
        }
        file2 = new File(file1, "level.dat_old");
        return file2.exists() ? SaveFormatOld.getWorldData(file2, this.dataFixer) : null;
    }

    @Nullable
    public static WorldInfo getWorldData(File p_186353_0_, DataFixer dataFixerIn) {
        try {
            NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(p_186353_0_));
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Data");
            return new WorldInfo(dataFixerIn.process(FixTypes.LEVEL, nbttagcompound1));
        }
        catch (Exception exception) {
            LOGGER.error("Exception reading {}", (Object)p_186353_0_, (Object)exception);
            return null;
        }
    }

    @Override
    public void renameWorld(String dirName, String newName) {
        File file2;
        File file1 = new File(this.savesDirectory, dirName);
        if (file1.exists() && (file2 = new File(file1, "level.dat")).exists()) {
            try {
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file2));
                NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Data");
                nbttagcompound1.setString("LevelName", newName);
                CompressedStreamTools.writeCompressed(nbttagcompound, new FileOutputStream(file2));
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public boolean isNewLevelIdAcceptable(String saveName) {
        File file1 = new File(this.savesDirectory, saveName);
        if (file1.exists()) {
            return false;
        }
        try {
            file1.mkdir();
            file1.delete();
            return true;
        }
        catch (Throwable throwable) {
            LOGGER.warn("Couldn't make new level", throwable);
            return false;
        }
    }

    @Override
    public boolean deleteWorldDirectory(String saveName) {
        File file1 = new File(this.savesDirectory, saveName);
        if (!file1.exists()) {
            return true;
        }
        LOGGER.info("Deleting level {}", (Object)saveName);
        int i = 1;
        while (i <= 5) {
            LOGGER.info("Attempt {}...", (Object)i);
            if (SaveFormatOld.deleteFiles(file1.listFiles())) break;
            LOGGER.warn("Unsuccessful in deleting contents.");
            if (i < 5) {
                try {
                    Thread.sleep(500L);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
            }
            ++i;
        }
        return file1.delete();
    }

    protected static boolean deleteFiles(File[] files) {
        File[] fileArray = files;
        int n = files.length;
        int n2 = 0;
        while (n2 < n) {
            File file1 = fileArray[n2];
            LOGGER.debug("Deleting {}", (Object)file1);
            if (file1.isDirectory() && !SaveFormatOld.deleteFiles(file1.listFiles())) {
                LOGGER.warn("Couldn't delete directory {}", (Object)file1);
                return false;
            }
            if (!file1.delete()) {
                LOGGER.warn("Couldn't delete file {}", (Object)file1);
                return false;
            }
            ++n2;
        }
        return true;
    }

    @Override
    public ISaveHandler getSaveLoader(String saveName, boolean storePlayerdata) {
        return new SaveHandler(this.savesDirectory, saveName, storePlayerdata, this.dataFixer);
    }

    @Override
    public boolean isConvertible(String saveName) {
        return false;
    }

    @Override
    public boolean isOldMapFormat(String saveName) {
        return false;
    }

    @Override
    public boolean convertMapFormat(String filename, IProgressUpdate progressCallback) {
        return false;
    }

    @Override
    public boolean canLoadWorld(String saveName) {
        File file1 = new File(this.savesDirectory, saveName);
        return file1.isDirectory();
    }

    @Override
    public File getFile(String p_186352_1_, String p_186352_2_) {
        return new File(new File(this.savesDirectory, p_186352_1_), p_186352_2_);
    }
}

