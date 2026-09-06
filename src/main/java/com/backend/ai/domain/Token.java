package com.backend.ai.domain;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;

/**
 * Domain model representing tokenized text output containing IDs, attention masks, and type IDs.
 */
public class Token {
    long[] ids;
    long[] attentionMask;
    long[] typeIds;

    /**
     * Constructs a Token instance using the provided Builder.
     *
     * @param builder the builder containing token configuration
     */
    public Token(Builder builder) {
        this.ids = builder.ids;
        this.attentionMask = builder.attentionMask;
        this.typeIds = builder.typeIds;
    }

    /**
     * Gets the token IDs array.
     *
     * @return array of token ID values
     */
    public long[] getIds() {
        return ids;
    }

    /**
     * Gets the attention mask array.
     *
     * @return array of attention mask values
     */
    public long[] getAttentionMask() {
        return attentionMask;
    }

    /**
     * Gets the token type IDs array.
     *
     * @return array of token type ID values
     */
    public long[] getTypeIds() {
        return this.typeIds;
    }

    /**
     * Converts token arrays into Deep Java Library (DJL) NDList tensor representations for model input.
     *
     * @return NDList containing input tensors (ids, attentionMask, typeIds)
     */
    public NDList getNDList() {
        // Create tensors from raw arrays. Necessary for embedding generation.
        NDManager manager = NDManager.newBaseManager();
        // Create Arrays with another dimension
        NDArray ids = manager.create(this.getIds()).expandDims(0);
        NDArray attentionMask = manager.create(this.getAttentionMask()).expandDims(0);
        NDArray typeIds = manager.create(this.getTypeIds()).expandDims(0);
        // Create a list of NDArray
        return new NDList(ids, attentionMask, typeIds);
    }

    /**
     * Creates a new Builder instance for constructing Token objects.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder pattern implementation for constructing {@link Token} instances.
     */
    public static class Builder {
        long[] ids;
        long[] attentionMask;
        long[] typeIds;

        /**
         * Sets the token IDs array.
         *
         * @param ids array of token IDs
         * @return this builder instance
         */
        public Builder ids(long[] ids) {
            this.ids = ids;
            return this;
        }

        /**
         * Sets the attention mask array.
         *
         * @param attentionMask array of attention mask values
         * @return this builder instance
         */
        public Builder attentionMask(long[] attentionMask) {
            this.attentionMask = attentionMask;
            return this;
        }

        /**
         * Sets the token type IDs array.
         *
         * @param typeIds array of token type IDs
         * @return this builder instance
         */
        public Builder typeIds(long[] typeIds) {
            this.typeIds = typeIds;
            return this;
        }

        /**
         * Builds and returns a new {@link Token} object.
         *
         * @return constructed Token instance
         */
        public Token build() {
            return new Token(this);
        }
    }
}
