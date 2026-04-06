package com.backend.ai.domain;

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
