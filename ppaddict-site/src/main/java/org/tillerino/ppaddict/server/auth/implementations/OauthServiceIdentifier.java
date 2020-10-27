package org.tillerino.ppaddict.server.auth.implementations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.annotation.meta.TypeQualifier;

@Retention(RetentionPolicy.RUNTIME)
@TypeQualifier
public @interface OauthServiceIdentifier {

}
