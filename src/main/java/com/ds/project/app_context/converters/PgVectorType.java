package com.ds.project.app_context.converters;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class PgVectorType implements UserType<float[]> {

    @Override
    public int getSqlType() {
        return Types.OTHER; // pgvector
    }

    @Override
    public Class<float[]> returnedClass() {
        return float[].class;
    }

    @Override
    public boolean equals(float[] x, float[] y) {
        return java.util.Arrays.equals(x, y);
    }

    @Override
    public int hashCode(float[] x) {
        return java.util.Arrays.hashCode(x);
    }

    @Override
    public float[] nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        String val = rs.getString(position);
        if (val == null) return null;

        String[] parts = val.replaceAll("[\\[\\]]", "").split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, float[] value, int index,
                            SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
            return;
        }

        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < value.length; i++) {
            builder.append(value[i]);
            if (i < value.length - 1) builder.append(", ");
        }
        builder.append("]");


        PGobject obj = new PGobject();
        obj.setType("vector");
        obj.setValue(builder.toString());


        st.setObject(index, obj);
    }

    @Override
    public float[] deepCopy(float[] value) {
        return value == null ? null : value.clone();
    }

    @Override public boolean isMutable() { return true; }
    @Override public Serializable disassemble(float[] value) { return deepCopy(value); }
    @Override public float[] assemble(Serializable cached, Object owner) { return deepCopy((float[]) cached); }
    @Override public float[] replace(float[] original, float[] target, Object owner) { return deepCopy(original); }
}

