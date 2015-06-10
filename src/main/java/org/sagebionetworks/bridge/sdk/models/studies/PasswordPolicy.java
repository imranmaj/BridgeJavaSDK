package org.sagebionetworks.bridge.sdk.models.studies;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class PasswordPolicy {
    
    private final int minLength;
    private final boolean numericRequired;
    private final boolean symbolRequired;
    private final boolean upperCaseRequired;
    
    @JsonCreator
    public PasswordPolicy(@JsonProperty("minLength") int minLength,
                    @JsonProperty("numericRequired") boolean requireNumeric,
                    @JsonProperty("symbolRequired") boolean requireSymbol,
                    @JsonProperty("UpperCaseRequired") boolean requireUpperCase) {
        this.minLength = minLength;
        this.numericRequired = requireNumeric;
        this.symbolRequired = requireSymbol;
        this.upperCaseRequired = requireUpperCase;
    }
    
    public int getMinLength() {
        return minLength;
    }
    public boolean isNumericRequired() {
        return numericRequired;
    }
    public boolean isSymbolRequired() {
        return symbolRequired;
    }
    public boolean isUpperCaseRequired() {
        return upperCaseRequired;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(minLength);
        result = prime * result + Objects.hashCode(numericRequired);
        result = prime * result + Objects.hashCode(symbolRequired);
        result = prime * result + Objects.hashCode(upperCaseRequired);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        PasswordPolicy other = (PasswordPolicy) obj;
        return (Objects.equals(minLength, other.minLength) && 
                Objects.equals(numericRequired, other.numericRequired) &&
                Objects.equals(symbolRequired, other.symbolRequired) &&
                Objects.equals(upperCaseRequired, other.upperCaseRequired));
    }

    @Override
    public String toString() {
        return String.format("PasswordPolicy [minLength=%s, numericRequired=%s, symbolRequired=%s, upperCaseRequired=%s]",
            minLength, numericRequired, symbolRequired, upperCaseRequired);
    }
}
