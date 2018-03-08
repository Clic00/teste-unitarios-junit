package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.utils.DataUtils.obterData;
import static builders.FilmeBuilder.umFilme;
import static builders.FilmeBuilder.umFilmeSemEstoque;
import static builders.LocacaoBuilder.umLocacao;
import static builders.UsuarioBuilder.umUsuario;
import static matchers.MatchersProprios.caiNumaSegunda;
import static matchers.MatchersProprios.ehHoje;
import static matchers.MatchersProprios.ehHojeComDiferencaDias;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.runners.ParallelRunner;
import br.ce.wcaquino.utils.DataUtils;

@RunWith(ParallelRunner.class)
public class LocacaoServiceTest {

	@InjectMocks @Spy
	private LocacaoService service;

	@Mock
	private LocacaoDAO dao;

	@Mock
	private SPCService spc;

	@Mock
	private EmailService email;

	private Filme f1;
	private Filme f2;
	private Filme f3;
	private Filme f4;
	private Filme f5;
	private Filme f6;
	private Filme f7;
	private Filme f8;
	private Filme f9;

	@Rule
	public ErrorCollector error = new ErrorCollector();

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void antes() {
		// service = new LocacaoService();
		// dao = mock(LocacaoDAO.class);
		// service.setLocacaoDAO(dao);
		// spc = mock(SPCService.class);
		// service.setSpcService(spc);
		// email = mock(EmailService.class);
		// service.setEmailService(email);
		// --------------\|/-----------
		// V
		MockitoAnnotations.initMocks(this);
		// ----------------------------
		
		f1 = umFilme().comValor(5.0).agora();
		f2 = umFilmeSemEstoque().agora();
		f3 = umFilmeSemEstoque().agora();
		f4 = umFilme().agora();
		f5 = umFilme().agora();
		f6 = umFilme().agora();
		f7 = umFilme().agora();
		f8 = umFilme().agora();
		f9 = umFilme().agora();
	}

	@Test
	public void deveAlugarFilme() throws Exception {

//		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		
		// Cenario

		Usuario user = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(f1);

		doReturn(obterData(9, 03, 2018)).when(service).obterData();
		
		// Acao

		Locacao locacao = service.alugarFilme(user, filmes);	

		// Verificacao
//		Assert.assertEquals(5.0, locacao.getValor(), 0.01);
//		assertTrue(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()));
//		assertTrue(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)));
		// testando usando Matchers
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), DataUtils.obterData(9, 03, 2018)), is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterData(10, 03, 2018)), is(true));

	}

	// Tratamento de exceções: caso onde é esperado uma exceção e a mesma não
	// ocorre...

	@Test(expected = FilmeSemEstoqueException.class)
	public void deveLancarExcecaoAoAlugarFilmeSemEstoque() throws Exception { // Forma elegante.

		// Cenario

		Usuario user = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(f1, f2, f3);

		// Acao
		service.alugarFilme(user, filmes);
	}

	@Test
	public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException { // *** Forma robusta.

		// Cenario

		List<Filme> filmes = Arrays.asList(f1, f2, f3);

		// Acao

		try {
			service.alugarFilme(null, filmes);
			Assert.fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuário vazio!"));
		}

	}

	@Test
	public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException { // Forma Nova

		// cenario
		Usuario user = umUsuario().agora();

		// Acao
		exception.expect(LocadoraException.class);
		exception.expectMessage("Filme vazio!");

		service.alugarFilme(user, null);
	}

	@Test
	public void devePagar75PCNoFilme3() throws FilmeSemEstoqueException, LocadoraException {
		// Cenario

		Usuario user = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(f4, f5, f6);

		// Acao
		Locacao resultado = service.alugarFilme(user, filmes);

		// Verificacao
		// 4+4+3=11
		assertThat(resultado.getValor(), is(11.0));

	}

	@Test
	public void devePagar50PCNoFilme4() throws FilmeSemEstoqueException, LocadoraException {
		// Cenario

		Usuario user = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(f4, f5, f6, f7);

		// Acao
		Locacao resultado = service.alugarFilme(user, filmes);

		// Verificacao
		// 4+4+3+2=13
		assertThat(resultado.getValor(), is(13.0));
	}

	@Test
	public void devePagar25PCNoFilme5() throws FilmeSemEstoqueException, LocadoraException {
		// Cenario

		Usuario user = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(f4, f5, f6, f7, f8);

		// Acao
		Locacao resultado = service.alugarFilme(user, filmes);

		// Verificacao
		// 4+4+3+2+=14
		assertThat(resultado.getValor(), is(14.0));
	}

	@Test
	public void devePagar0PCNoFilme6() throws FilmeSemEstoqueException, LocadoraException {
		// Cenario

		Usuario user = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(f4, f5, f6, f7, f8, f9);

		// Acao
		Locacao resultado = service.alugarFilme(user, filmes);

		// Verificacao
		// 4+4+3+2+0=14
		assertThat(resultado.getValor(), is(14.0));
	}

	@Test
	public void deveDevolverNaSegundaAoAlugarNoSabado() throws Exception {
		// cenario
		Usuario user = umUsuario().agora();	
		List<Filme> filmes = Arrays.asList(f4, f5);
		
		doReturn(obterData(10, 03, 2018)).when(service).obterData();
		
		// Acao
		Locacao retorno = service.alugarFilme(user, filmes);

		// Verificacao
		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
	}

	// public static void main(String[] args) {
	// new BuilderMaster().gerarCodigoClasse(Locacao.class);
	// }
	@Test
	public void naoDeveAlugarFilmeParaNegativadoSPC() throws Exception {
		// cenario

		Usuario user = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(f1);

		when(spc.possuiNegativacao(user)).thenReturn(true);

		// Acao
		try {
			service.alugarFilme(user, filmes);
			// Verificacao
			Assert.fail();
		} catch (LocadoraException e) {
			Assert.assertThat(e.getMessage(), is("Usuário negativado!"));
		}
	}

	@Test
	public void deveEnviarEmailParaLocacoesAtrasadas() throws FilmeSemEstoqueException, LocadoraException {
		// cenario

		Usuario user = umUsuario().agora();
		Usuario user2 = umUsuario().comNome("Usuario em dia").agora();
		Usuario user3 = umUsuario().comNome("Outro atrasado").agora();
		List<Locacao> locacoes = Arrays.asList(umLocacao().atrasada().comUsuario(user).agora(),
				umLocacao().comUsuario(user2).agora(), umLocacao().atrasada().comUsuario(user3).agora(),
				umLocacao().atrasada().comUsuario(user3).agora());

		when(dao.obterLocacoesPendentes()).thenReturn(locacoes);

		// Acao
		service.notificarAtrasos();

		// Verificacao
//		ArgumentCaptor<Usuario> argumentCaptor = ArgumentCaptor.forClass(Usuario.class);
		verify(email, times(3)).notificarAtraso(Mockito.any(Usuario.class));
		verify(email, atLeastOnce()).notificarAtraso(user);// informa que pelo menos uma vez deve ser informado
		verify(email, never()).notificarAtraso(user2);
		verify(email, times(2)).notificarAtraso(user3); // informa a quantdades de vezes que deve ocorrer
		verifyNoMoreInteractions(email);
		verifyZeroInteractions(spc);
//		verify(email).notificarAtraso(argumentCaptor.capture());
		
//		System.out.println(argumentCaptor.getValue());
		
	}

	@Test
	public void deveTratarErroNoSPC() throws Exception {
		// cenario
		Usuario user = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(f1, f4);

		when(spc.possuiNegativacao(user)).thenThrow(new Exception("Falha catastrófica!"));

		// Verificação
		exception.expect(LocadoraException.class);
		exception.expectMessage("SPC fora do ar, tente novamente!");

		// Ação

		service.alugarFilme(user, filmes);
	}
	
	@Test
	public void deveProrrogarUmaLocacao() {
		//cenario
		Locacao locacao = umLocacao().agora();
		
		//Acao
		service.prorrogarLocacao(locacao, 3);
		
		//Verificacao
		ArgumentCaptor<Locacao> argumentCaptor = ArgumentCaptor.forClass(Locacao.class);
		verify(dao).salvar(argumentCaptor.capture());
		Locacao locacaoRetornada = argumentCaptor.getValue();
		
		error.checkThat(locacaoRetornada.getValor(), is(12.0));
		error.checkThat(locacaoRetornada.getDataLocacao(), ehHoje());
		error.checkThat(locacaoRetornada.getDataRetorno(), ehHojeComDiferencaDias(3));
		
	}
	
	@Test
	public void deveCalcularValorLocacao() throws Exception {
		//Cenario
		List<Filme> filmes = Arrays.asList(f2);		
		
		//Acao
		Class<LocacaoService> clazz = LocacaoService.class;
		Method metodo = clazz.getDeclaredMethod("calcularValorLocacao", List.class);
		metodo.setAccessible(true);
		Double valor = (Double) metodo.invoke(service, filmes);
		
		
		//Verificacao
		Assert.assertThat(valor, is(4.0));
		
	}
	

}