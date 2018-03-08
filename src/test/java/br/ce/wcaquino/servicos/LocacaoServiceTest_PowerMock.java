package br.ce.wcaquino.servicos;

import static builders.FilmeBuilder.umFilme;
import static builders.FilmeBuilder.umFilmeSemEstoque;
import static builders.UsuarioBuilder.umUsuario;
import static matchers.MatchersProprios.caiNumaSegunda;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.utils.DataUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LocacaoService.class})
public class LocacaoServiceTest_PowerMock {

	@InjectMocks
	private LocacaoService service;

	@Mock
	private LocacaoDAO dao;

	@Mock
	private SPCService spc;

	@Mock
	private EmailService email;

	private Filme f1;
	private Filme f2;
	private Filme f4;
	private Filme f5;

	@Rule
	public ErrorCollector error = new ErrorCollector();

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void antes() {
		MockitoAnnotations.initMocks(this);
		
		service = PowerMockito.spy(service);
		
		f1 = umFilme().comValor(5.0).agora();
		f2 = umFilmeSemEstoque().agora();
		f4 = umFilme().agora();
		f5 = umFilme().agora();
	}

	@Test
	public void deveAlugarFilme() throws Exception {

		// Cenario

		Usuario user = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(f1);
		
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(9, 03, 2018));
		
		// Acao

		Locacao locacao = service.alugarFilme(user, filmes);

		// Verificacao
		error.checkThat(locacao.getValor(), is(equalTo(5.0)));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), DataUtils.obterData(9, 03, 2018)), is(true));
		error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterData(10, 03, 2018)), is(true));

	}

	@Test
	public void deveDevolverNaSegundaAoAlugarNoSabado() throws Exception {
		// cenario
		Usuario user = umUsuario().agora();	
		List<Filme> filmes = Arrays.asList(f4, f5);
		
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(10, 03, 2018));

		// Acao
		Locacao retorno = service.alugarFilme(user, filmes);

		// Verificacao
		assertThat(retorno.getDataRetorno(), caiNumaSegunda());	
	}

	@Test
	public void deveAlugarFilmeSemCalcularValor() throws Exception {
		//Cenario
		Usuario user = umUsuario().agora();
		List<Filme> filmes = Arrays.asList(f1, f4);
		
		PowerMockito.doReturn(1.0).when(service, "calcularValorLocacao", filmes);
		
		//Acao
		Locacao locacao = service.alugarFilme(user, filmes);
		
		//Verificacao
		Assert.assertThat(locacao.getValor(), is(1.0));
		PowerMockito.verifyPrivate(service).invoke( "calcularValorLocacao", filmes);
	}
	
	@Test
	public void deveCalcularValorLocacao() throws Exception {
		//Cenario
		List<Filme> filmes = Arrays.asList(f2);		
		
		//Acao
		Double valor = (Double) Whitebox.invokeMethod(service,  "calcularValorLocacao", filmes);
		
		//Verificacao
		Assert.assertThat(valor, is(4.0));	
	}
}