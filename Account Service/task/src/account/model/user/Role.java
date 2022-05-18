package account.model.user;

public enum Role {
    ADMINISTRATOR,
    AUDITOR,
    ACCOUNTANT,
    USER;

    public String withPrefix() {
        return "ROLE_" + this.name();
    }
}
