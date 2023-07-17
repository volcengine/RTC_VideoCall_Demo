package com.volcengine.vertcdemo.videocall.effect;

import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.effect.model.Effect;
import com.volcengine.vertcdemo.videocall.effect.model.EffectNode;
import com.volcengine.vertcdemo.videocall.effect.model.Effects;

/**
 * demo使用到的美颜数据组装工具类
 */
public class EffectDataUtil {

    /**
     * 初始化美颜数据
     */
    public static Effects createBeauty() {
        Effects beauty = new Effects(R.string.beauty, false, Effect.TYPE_BEAUTY);
        EffectNode none = new EffectNode(R.string.effect_none, R.drawable.ic_effect_no_select, null, EffectMaterialUtil.getBeautyPath(), false, Effect.TYPE_NONE, beauty);
        EffectNode whiten = new EffectNode(R.string.whitening, R.drawable.ic_effect_whiten, "whiten", EffectMaterialUtil.getBeautyPath(), true, 0.7f, Effect.TYPE_BEAUTY, beauty);
        EffectNode smooth = new EffectNode(R.string.smooth, R.drawable.ic_effect_smooth, "smooth", EffectMaterialUtil.getBeautyPath(), true, 0.8f, Effect.TYPE_BEAUTY, beauty);
        EffectNode sharp = new EffectNode(R.string.sharp, R.drawable.ic_effect_sharp, "sharp", EffectMaterialUtil.getBeautyPath(), true, 0.5f, Effect.TYPE_BEAUTY, beauty);
        EffectNode clear = new EffectNode(R.string.clear, R.drawable.ic_effect_clear, "clear", EffectMaterialUtil.getBeautyPath(), true, 0.7f, Effect.TYPE_BEAUTY, beauty);
        beauty.addChildren(none, whiten, smooth, sharp, clear);
        whiten.setSelected(true);

        return beauty;
    }

    /**
     * 初始化美型数据
     */
    public static Effects createReshape() {
        Effects reshape = new Effects(R.string.reshape, false, Effect.TYPE_RESHAPE);
        EffectNode none = new EffectNode(R.string.effect_none, R.drawable.ic_effect_no_select, null, null, false, Effect.TYPE_NONE, reshape);
        EffectNode bigEye = new EffectNode(R.string.big_eyes, R.drawable.ic_effect_big_eye, "Internal_Deform_Eye", EffectMaterialUtil.getLiteReshapePath(), true, 0.3f, Effect.TYPE_RESHAPE, reshape);

        Effects slimFace = new Effects(R.string.thin_face, R.drawable.ic_effect_slim_face, true, Effect.TYPE_RESHAPE, reshape);
        EffectNode slimeFaceNote = new EffectNode(R.string.thin_face, R.drawable.ic_effect_slim_face, "Internal_Deform_Overall", EffectMaterialUtil.getLiteReshapePath(), true, Effect.TYPE_RESHAPE, slimFace);
        EffectNode boyFaceNote = new EffectNode(R.string.boy_face, R.drawable.ic_effect_boy, "Internal_Deform_Overall", EffectMaterialUtil.getBoyReshapePath(), true, Effect.TYPE_RESHAPE, slimFace);
        EffectNode girlFaceNote = new EffectNode(R.string.gril_face, R.drawable.ic_effect_gril, "Internal_Deform_Overall", EffectMaterialUtil.getGirlReshapePath(), true, Effect.TYPE_RESHAPE, slimFace);
        EffectNode natureFaceNote = new EffectNode(R.string.natural_face, R.drawable.ic_effect_nature, "Internal_Deform_Overall", EffectMaterialUtil.getNatureReshapePath(), true, 0.8f, Effect.TYPE_RESHAPE, slimFace);
        natureFaceNote.setSelected(true);
        slimFace.addChildren(slimeFaceNote, boyFaceNote, girlFaceNote, natureFaceNote);

        EffectNode cutFace = new EffectNode(R.string.cut_face, R.drawable.ic_effect_narrow_face, "Internal_Deform_CutFace", EffectMaterialUtil.getLiteReshapePath(), true, Effect.TYPE_RESHAPE, reshape);
        EffectNode cheekbone = new EffectNode(R.string.cheekbone, R.drawable.ic_effect_cheekbone, "Internal_Deform_Zoom_Cheekbone", EffectMaterialUtil.getLiteReshapePath(), true, Effect.TYPE_RESHAPE, reshape);
        EffectNode jawbone = new EffectNode(R.string.jawbone, R.drawable.ic_effect_jawbone, "Internal_Deform_Zoom_Jawbone", EffectMaterialUtil.getLiteReshapePath(), true, Effect.TYPE_RESHAPE, reshape);
        EffectNode noseWing = new EffectNode(R.string.nose_wing, R.drawable.ic_effect_nose, "Internal_Deform_Nose", EffectMaterialUtil.getLiteReshapePath(), true, 0.5f, Effect.TYPE_RESHAPE, reshape);
        EffectNode smilesFolds = new EffectNode(R.string.smiles_folds, R.drawable.ic_effect_smiles_folds, "BEF_BEAUTY_SMILES_FOLDS", EffectMaterialUtil.get4ItemReshapePath(), true, 0.7f, Effect.TYPE_RESHAPE, reshape);
        EffectNode removePouch = new EffectNode(R.string.remove_pouch, R.drawable.ic_effect_remove_pouch, "BEF_BEAUTY_REMOVE_POUCH", EffectMaterialUtil.get4ItemReshapePath(), true, 0.6f, Effect.TYPE_RESHAPE, reshape);

        reshape.addChildren(none, bigEye, slimFace, cutFace, cheekbone, jawbone, noseWing, smilesFolds, removePouch);
        none.setSelected(true);
        return reshape;
    }

    /**
     * 初始化过滤器数据
     */
    public static Effects createFilter() {
        Effects filters = new Effects(R.string.filter, true, Effect.TYPE_FILTER);
        EffectNode none = new EffectNode(R.string.effect_none, R.drawable.ic_effect_no_select, null, null, false, Effect.TYPE_NONE, filters);

        Effects portrait = new Effects(R.string.portrait, R.drawable.ic_effect_filter_portrait, true, Effect.TYPE_FILTER, filters);
        EffectNode coldWhite = new EffectNode(R.string.lengbaipi, R.drawable.ic_effect_filter_cold_white, "Filter_48_4001", EffectMaterialUtil.getFilterPath(), true, Effect.TYPE_FILTER, portrait);
        EffectNode nature = new EffectNode(R.string.nature, R.drawable.ic_effect_filter_nature, "Filter_38_F1", EffectMaterialUtil.getFilterPath(), true, Effect.TYPE_FILTER, portrait);
        EffectNode light = new EffectNode(R.string.touliang, R.drawable.ic_effect_filter_bright, "Filter_49_4002", EffectMaterialUtil.getFilterPath(), true, Effect.TYPE_FILTER, portrait);
        EffectNode degula = new EffectNode(R.string.degula, R.drawable.ic_effect_filter_dracula, "Filter_53_4006", EffectMaterialUtil.getFilterPath(), true, Effect.TYPE_FILTER, portrait);
        EffectNode lightOrange = new EffectNode(R.string.liangju, R.drawable.ic_effect_filter_bright_orange, "Filter_50_4003", EffectMaterialUtil.getFilterPath(), true, Effect.TYPE_FILTER, portrait);
        EffectNode milkTea = new EffectNode(R.string.naicha, R.drawable.ic_effect_filter_milk_tea, "Filter_27_Po5", EffectMaterialUtil.getFilterPath(), true, Effect.TYPE_FILTER, portrait);
        portrait.addChildren(coldWhite, nature, light, degula, lightOrange, milkTea);

        Effects food = new Effects(R.string.food, R.drawable.ic_effect_filter_food, true, Effect.TYPE_FILTER, filters);
        EffectNode water = new EffectNode(R.string.qipaoshui, R.drawable.ic_effect_filter_sparkling_water, "Filter_57_4010", EffectMaterialUtil.getFilterPath(), true, Effect.TYPE_FILTER, food);
        food.addChildren(water);

        Effects landscape = new Effects(R.string.landscape, R.drawable.ic_effect_filter_landscape, true, Effect.TYPE_FILTER, filters);
        EffectNode northSeaRoad = new EffectNode(R.string.beihaidao, R.drawable.ic_effect_filter_hokkaido, "Filter_12_08", EffectMaterialUtil.getFilterPath(), true, Effect.TYPE_FILTER, landscape);
        EffectNode green = new EffectNode(R.string.lvyan, R.drawable.ic_effect_filter_green, "Filter_62_4015", EffectMaterialUtil.getFilterPath(), true, Effect.TYPE_FILTER, landscape);
        landscape.addChildren(northSeaRoad, green);

        Effects vintage = new Effects(R.string.vintage, R.drawable.ic_effect_filter_vintage, true, Effect.TYPE_FILTER, filters);
        EffectNode film = new EffectNode(R.string.meishijiaopian, R.drawable.ic_effect_filter_american_film, "Filter_43_S1", EffectMaterialUtil.getFilterPath(), true, Effect.TYPE_FILTER, vintage);
        vintage.addChildren(film);

        filters.addChildren(none, portrait, food, landscape, vintage);
        none.setSelected(true);
        return filters;
    }

}
