package br.com.star.wars;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.totvs.tjf.core.common.security.SecurityPrincipal;

public class StarWarsHabitantsAuthentication {

	public static void setAuthenticationInfo(String tenant) {
		// A classe SecurityPrincipal recebe três parâmetros:
		// 1 - Código do usuário, exemplo: admin
		// 2 - Código do tenant, exemplo: 92e8a7dc-61d8-4045-9d80-222c774ad790
		// 3 - Código do tenant que será salvo no banco de dados, exemplo: 92e8a7dc
		SecurityPrincipal principal = new SecurityPrincipal("admin", tenant, tenant);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, "");
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

}
