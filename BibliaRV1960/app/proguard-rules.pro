# Room
-keep class com.mi.bibliarv1960.data.local.entities.** { *; }
-keep interface com.mi.bibliarv1960.data.local.dao.** { *; }

# Serialization
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepclassmembers class com.mi.bibliarv1960.ui.challenges.DailyChallengeStatus { *; }
-keepclassmembers class com.mi.bibliarv1960.ui.challenges.ChallengeResult* { *; }
