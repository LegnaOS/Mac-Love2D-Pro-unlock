# Upstream and scope

Based on https://github.com/ailidev/LoveLive2D at commit
`0f8337c054f70f2e225ae176c173ac73afe6a0a1` (retrieved 2026-10-24).
The original README credits kiraio-moe. Upstream license: GPL-3.0, retained in LICENSE.
`upstream/rlm1501/*.java` are unmodified upstream source files.
The Mac UI and Java transaction wrapper are new adaptations, distributed under GPL-3.0.

The rlm1501 profile retains the upstream approach. The experimental rlm1603
profile is restricted to two exact SHA256 values for Cubism 5.3.04 (see README).
It replaces only RlmLicense.class and its hash reference in h.class, preserving
the remaining 1603 classes and native library. It never renames the whole 1501
replacement archive to 1603. The adapted class adds lifecycle handling and
guards unused native attribute methods against a zero handle.

Observed 1603 runtime state is an extended trial, not a PRO activation. The
isolated Java-entry test opened/saved a model, generated an atlas and exported
moc3; native app launching and full feature coverage are separate acceptance items.
No Cubism editor binaries, commercial SDKs, user projects or credentials are packaged.
