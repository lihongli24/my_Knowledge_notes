package com.demo.li.lombok;

/**
 * lombol测试子类
 *
 * @author lihongli
 * create：2020/4/22 7:23 下午
 */
public class StudentWithEquals extends Person {
    /**
     * 所在学校
     */
    private String school;

    public StudentWithEquals() {
    }

    public String getSchool() {
        return this.school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String toString() {
        return "StudentWithEquals(school=" + this.getSchool() + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof StudentWithEquals)) {
            return false;
        }
        final StudentWithEquals other = (StudentWithEquals) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final Object this$school = this.getSchool();
        final Object other$school = other.getSchool();
        if (this$school == null ? other$school != null : !this$school.equals(other$school)) {
            return false;
        }
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof StudentWithEquals;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final Object $school = this.getSchool();
        result = result * PRIME + ($school == null ? 43 : $school.hashCode());
        return result;
    }
}
