/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.bluecolored.bluemap.core.resources.resourcepack.blockstate;

import com.google.gson.Gson;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import de.bluecolored.bluemap.api.debug.DebugDump;
import de.bluecolored.bluemap.core.resources.AbstractTypeAdapterFactory;
import de.bluecolored.bluemap.core.resources.adapter.ResourcesGson;
import de.bluecolored.bluemap.core.world.BlockState;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("FieldMayBeFinal")
@DebugDump
@JsonAdapter(Multipart.Adapter.class)
public class Multipart {

    private VariantSet[] parts = new VariantSet[0];

    private Multipart(){}

    public VariantSet[] getParts() {
        return parts;
    }

    public void forEach(BlockState blockState, int x, int y, int z, Consumer<Variant> consumer) {
        for (VariantSet part : parts) {
            if (part.getCondition().matches(blockState)) {
                part.forEach(x, y, z, consumer);
            }
        }
    }

    static class Adapter extends AbstractTypeAdapterFactory<Multipart> {

        public Adapter() {
            super(Multipart.class);
        }

        @Override
        public Multipart read(JsonReader in, Gson gson) throws IOException {
            List<VariantSet> parts = new ArrayList<>();

            in.beginArray();
            while (in.hasNext()) {
                VariantSet variantSet = null;
                BlockStateCondition condition = null;

                in.beginObject();
                while (in.hasNext()) {
                    String key = in.nextName();
                    switch (key) {
                        case "when": condition = readCondition(in); break;
                        case "apply": variantSet = gson.fromJson(in, VariantSet.class); break;
                        default: in.skipValue(); break;
                    }
                }
                in.endObject();

                if (variantSet == null) continue;
                if (condition != null) variantSet.setCondition(condition);
                parts.add(variantSet);
            }
            in.endArray();

            Multipart result = new Multipart();
            result.parts = parts.toArray(new VariantSet[0]);
            return result;
        }

        public BlockStateCondition readCondition(JsonReader in) throws IOException {
            List<BlockStateCondition> andConditions = new ArrayList<>();
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equals(JSON_COMMENT)) {
                    in.skipValue();
                    continue;
                }

                if (name.equals("OR")) {
                    List<BlockStateCondition> orConditions = new ArrayList<>();
                    in.beginArray();
                    while (in.hasNext()) {
                        orConditions.add(readCondition(in));
                    }
                    in.endArray();
                    andConditions.add(
                            BlockStateCondition.or(orConditions.toArray(new BlockStateCondition[0])));
                } else if (name.equals("AND")) {
                    List<BlockStateCondition> andArray = new ArrayList<>();
                    in.beginArray();
                    while (in.hasNext()) {
                        andArray.add(readCondition(in));
                    }
                    in.endArray();
                    andConditions.add(
                            BlockStateCondition.and(andArray.toArray(new BlockStateCondition[0])));
                } else {
                    String[] values = StringUtils.split(ResourcesGson.nextStringOrBoolean(in), '|');
                    andConditions.add(BlockStateCondition.property(name, values));
                }
            }
            in.endObject();

            return BlockStateCondition.and(andConditions.toArray(new BlockStateCondition[0]));
        }

    }

}
