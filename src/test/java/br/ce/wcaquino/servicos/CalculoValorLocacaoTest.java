package br.ce.wcaquino.servicos;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;

/***
 * 
 * @author José:
 * @category Data Drive Tests
 *
 */

@RunWith(Parameterized.class)
public class CalculoValorLocacaoTest {

	@InjectMocks
	private LocacaoService service;

	@Mock
	private LocacaoDAO dao;

	@Mock
	private SPCService spc;

	@Parameter
	public List<Filme> filmes;

	@Parameter(value = 1)
	public Double valorLocacao;

	@Parameter(value = 2)
	public String cenario;

	@Before
	public void antes() {
		MockitoAnnotations.initMocks(this);
	}

	private static Filme f1 = new Filme("Star Wars", 2, 4.0);
	private static Filme f2 = new Filme("Exodo", 2, 4.0);
	private static Filme f3 = new Filme("Dez Mandamentos", 2, 4.0);
	private static Filme f4 = new Filme("Desconto 25%", 2, 4.0);
	private static Filme f5 = new Filme("Desconto 50%", 2, 4.0);
	private static Filme f6 = new Filme("Desconto 75%", 2, 4.0);
	private static Filme f7 = new Filme("Desconto 100%", 2, 4.0);

	@Parameters(name = "{2}")
	public static Collection<Object[]> getParametros() {

		return Arrays.asList(new Object[][] { { Arrays.asList(f1, f2), 8.0, "2 Filmes: Sem Desconto" },
				{ Arrays.asList(f1, f2, f3), 11.0, "3 filmes: 25%" },
				{ Arrays.asList(f1, f2, f3, f4), 13.0, "4 filmes: 50%" },
				{ Arrays.asList(f1, f2, f3, f4, f5), 14.0, "5 filmes: 75%" },
				{ Arrays.asList(f1, f2, f3, f4, f5, f6), 14.0, "6 filmes: 100%" },
				{ Arrays.asList(f1, f2, f3, f4, f5, f6, f7), 18.0, "7 Filmes: Sem Desconto" } });
	}

	@Test
	public void deveCalcularValorLocacaoConsiderandoDescontos() throws FilmeSemEstoqueException, LocadoraException {
		// Cenario
		Usuario user = new Usuario("Jose");

		// Acao
		Locacao resultado = service.alugarFilme(user, filmes);

		// Verificacao
		assertThat(resultado.getValor(), is(valorLocacao));
	}
}
