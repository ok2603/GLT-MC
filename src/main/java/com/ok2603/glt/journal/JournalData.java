package com.ok2603.glt.journal;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

/**
 * The journal's contents, saved with the world (data/glt_journal.dat).
 * There is one journal per world, shared by Chito and Yuuri: it's the story of their trip.
 */
public class JournalData extends SavedData {
    private static final String FILE = "glt_journal";
    private static final Factory<JournalData> FACTORY = new Factory<>(JournalData::new, JournalData::load, null);

    private final List<JournalEntry> entries = new ArrayList<>();

    public static JournalData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, FILE);
    }

    public List<JournalEntry> entries() {
        return List.copyOf(entries);
    }

    public boolean has(JournalMoment moment) {
        return entries.stream().anyMatch(entry -> entry.moment().equals(moment.id()));
    }

    public void add(JournalEntry entry) {
        entries.add(entry);
        setDirty();
    }

    public void clear() {
        entries.clear();
        setDirty();
    }

    private static JournalData load(CompoundTag tag, HolderLookup.Provider registries) {
        JournalData data = new JournalData();
        ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            data.entries.add(new JournalEntry(entry.getString("Moment"), entry.getString("Text"), entry.getInt("Day")));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (JournalEntry entry : entries) {
            CompoundTag saved = new CompoundTag();
            saved.putString("Moment", entry.moment());
            saved.putString("Text", entry.text());
            saved.putInt("Day", entry.day());
            list.add(saved);
        }
        tag.put("Entries", list);
        return tag;
    }
}
