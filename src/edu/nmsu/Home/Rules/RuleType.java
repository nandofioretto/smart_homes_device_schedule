package edu.nmsu.Home.Rules;

import java.util.HashMap;
import java.util.Map;

public enum RuleType {
    passive(0), active(1);

    private int ruleTypeNo;

    private static Map<Integer, RuleType> map = new HashMap<>();

    static {
        for (RuleType ruleTypeEnum : RuleType.values()) {
            map.put(ruleTypeEnum.ruleTypeNo, ruleTypeEnum);
        }
    }

    private RuleType(final int rule) {ruleTypeNo = rule; }

    public static RuleType valueOf(int ruleNo) {
        return map.get(ruleNo);
    }
}
