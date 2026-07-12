package rpg.extra.achievement.model;

/**
 * Static achievement definition loaded from {@code achievements.yml} (SOW
 * AchievementModule). {@code conditionValue} is interpreted per {@link ConditionType}: a
 * level number, a quest id, or a money amount.
 */
public final class AchievementDefinition {

    public enum ConditionType {
        REACH_LEVEL, COMPLETE_QUEST, MONEY_BALANCE
    }

    private final String id;
    private final String name;
    private final String description;
    private final ConditionType conditionType;
    private final String conditionValue;
    private final int rewardSkillPoints;

    public AchievementDefinition(String id, String name, String description, ConditionType conditionType,
                                  String conditionValue, int rewardSkillPoints) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
        this.rewardSkillPoints = rewardSkillPoints;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public int getRewardSkillPoints() {
        return rewardSkillPoints;
    }
}
