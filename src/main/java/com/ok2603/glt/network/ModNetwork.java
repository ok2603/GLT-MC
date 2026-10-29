package com.ok2603.glt.network;

import com.ok2603.glt.GLTMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Registers the messages the server and client send each other. */
@EventBusSubscriber(modid = GLTMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetwork {
    /** The latest body heat the server told this client about (only used on the client). */
    public static float clientBodyHeat = 100f;

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(BodyHeatPayload.TYPE, BodyHeatPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> clientBodyHeat = payload.heat()));
    }
}
