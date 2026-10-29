package com.ok2603.glt.block;

import com.mojang.serialization.MapCodec;
import com.ok2603.glt.journal.Journal;
import com.ok2603.glt.journal.JournalMoment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.Direction;

/**
 * Rain music: an empty can, an upturned bucket or Yuuri's helmet set out in the open.
 * When rain falls on it, it plays its note now and then, like raindrops pinging on metal.
 * Right-click to tune it (one step higher, sneak for lower), like a note block.
 * Set out several with different notes and the rain plays a song.
 *
 * Place a can by using it on the ground. Place a bucket or the helmet with sneak + right-click.
 */
public class RainCatcherBlock extends Block {
    public static final MapCodec<RainCatcherBlock> CODEC = simpleCodec(RainCatcherBlock::new);

    public enum Kind implements StringRepresentable {
        CAN("can", SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE, Block.box(5, 0, 5, 11, 7, 11)),
        BUCKET("bucket", SoundEvents.NOTE_BLOCK_BELL, Block.box(3, 0, 3, 13, 10, 13)),
        HELMET("helmet", SoundEvents.NOTE_BLOCK_COW_BELL, Block.box(3, 0, 3, 13, 6, 13));

        private final String name;
        private final Holder<SoundEvent> sound;
        private final VoxelShape shape;

        Kind(String name, Holder<SoundEvent> sound, VoxelShape shape) {
            this.name = name;
            this.sound = sound;
            this.shape = shape;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final EnumProperty<Kind> KIND = EnumProperty.create("kind", Kind.class);
    public static final IntegerProperty NOTE = BlockStateProperties.NOTE;

    public RainCatcherBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(KIND, Kind.CAN).setValue(NOTE, 12));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(KIND, NOTE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(KIND).shape;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, net.minecraft.world.level.LevelAccessor level,
                                     BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !canSurvive(state, level, pos)
                ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    /** Right-click: tune it up one step (sneak: down) and hear the note. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            int note = Math.floorMod(state.getValue(NOTE) + (player.isShiftKeyDown() ? -1 : 1), 25);
            BlockState tuned = state.setValue(NOTE, note);
            level.setBlock(pos, tuned, 3);
            play((ServerLevel) level, pos, tuned, 1.0f);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) {
            level.scheduleTick(pos, this, 20);
        }
    }

    /** Every so often while it rains on it, it plays its note. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isRainingAt(pos.above())) {
            play(level, pos, state, 0.5f + random.nextFloat() * 0.3f);
            if (random.nextInt(8) == 0) {
                for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new net.minecraft.world.phys.AABB(pos).inflate(8))) {
                    Journal.unlock(player, JournalMoment.RAIN_MUSIC);
                }
            }
            level.scheduleTick(pos, this, 12 + random.nextInt(30));
        } else {
            level.scheduleTick(pos, this, 100);
        }
    }

    private static void play(ServerLevel level, BlockPos pos, BlockState state, float volume) {
        int note = state.getValue(NOTE);
        level.playSound(null, pos, state.getValue(KIND).sound.value(), SoundSource.RECORDS, volume, NoteBlock.getPitchFromNote(note));
        level.sendParticles(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 0, note / 24.0, 0, 0, 1);
    }
}
