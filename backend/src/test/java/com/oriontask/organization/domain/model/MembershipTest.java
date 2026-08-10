package com.oriontask.organization.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MembershipTest {
  @Test
  void appliesFixedMembershipRevocationMatrix() {
    assertThat(Membership.Role.OWNER.canRevoke(Membership.Role.ADMIN)).isTrue();
    assertThat(Membership.Role.OWNER.canRevoke(Membership.Role.TECHNICIAN)).isTrue();
    assertThat(Membership.Role.OWNER.canRevoke(Membership.Role.OWNER)).isFalse();
    assertThat(Membership.Role.ADMIN.canRevoke(Membership.Role.TECHNICIAN)).isTrue();
    assertThat(Membership.Role.ADMIN.canRevoke(Membership.Role.ADMIN)).isFalse();
    assertThat(Membership.Role.TECHNICIAN.canRevoke(Membership.Role.TECHNICIAN)).isFalse();
  }
}
