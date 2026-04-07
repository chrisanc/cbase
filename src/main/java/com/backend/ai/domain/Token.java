package com.backend.ai.domain;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;

public class Token {
    long[] ids;
    long[] attentionMask;
    long[] typeIds;

    public Token(Builder builder) {
        this.ids = builder.ids;
        this.attentionMask = builder.attentionMask;
        this.typeIds = builder.typeIds;
    }

    public long[] getIds() {
        return ids;
    }

    public long[] getAttentionMask() {
        return attentionMask;
    }

    public long[] getTypeIds() {
        return this.typeIds;
    }

    public NDList getNDList() {
        // Create tensors from raw arrays. Necessary for the embedding generation.
        NDManager manager = NDManager.newBaseManager();
        // Create Arrays with another dimension
        NDArray ids = manager.create(this.getIds()).expandDims(0);
        NDArray attentionMask = manager.create(this.getAttentionMask()).expandDims(0);
        NDArray typeIds = manager.create(this.getTypeIds()).expandDims(0);
        // Create a list of NDArray
        return new NDList(ids, attentionMask, typeIds);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        long[] ids;
        long[] attentionMask;
        long[] typeIds;

        public Builder ids(long[] ids) {
            this.ids = ids;
            return this;
        }

        public Builder attentionMask(long[] attentionMask) {
            this.attentionMask = attentionMask;
            return this;
        }

        public Builder typeIds(long[] typeIds) {
            this.typeIds = typeIds;
            return this;
        }

        public Token build() {
            return new Token(this);
        }
    }
}
