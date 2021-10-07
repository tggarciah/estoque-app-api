package br.com.alura.estoque.repository;

import android.content.Context;

import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.EstoqueDatabase;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.callback.CallbackComRetorno;
import br.com.alura.estoque.retrofit.callback.CallbackSemRetorno;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;

public class ProdutoRepository {

   public static final String TAG = "ProdutoRepository";
   private final ProdutoDAO dao;
   private final ProdutoService service;

   public ProdutoRepository(Context context) {
      EstoqueDatabase db = EstoqueDatabase.getInstance(context);
      this.dao = db.getProdutoDAO();
      this.service = new EstoqueRetrofit().getProdutoService();
   }

   public void buscaProdutos(DadosCarregadosCallback<List<Produto>> callback) {
      buscaProdutosInternos(callback);
   }

   public void salva(final Produto produto, final DadosCarregadosCallback<Produto> callback) {
      salvaNaApi(produto, callback);
   }

   private void salvaNaApi(Produto produto, DadosCarregadosCallback<Produto> callback) {
      Call<Produto> call = service.salva(produto);
      call.enqueue(new CallbackComRetorno<>(new CallbackComRetorno.RespostaCallback<Produto>() {
         @Override
         public void quandoSucesso(Produto produtoSalvo) {
            salvaInterno(produtoSalvo, callback);
         }

         @Override
         public void quandoFalha(String erro) {
            callback.quandoFalha(erro);
         }
      }));
   }

   private void salvaInterno(Produto produtoSalvo, DadosCarregadosCallback<Produto> callback) {
      new BaseAsyncTask<>(() -> {
         long id = dao.salva(produtoSalvo);
         return dao.buscaProduto(id);
      }, callback::quandoSucesso).execute();
   }

   private void buscaProdutosInternos(DadosCarregadosCallback<List<Produto>> callback) {
      new BaseAsyncTask<>(dao::buscaTodos,
              produtosEncontrados -> {
                 callback.quandoSucesso(produtosEncontrados);
                 buscaProdutosAPI(callback);
              }).execute();
   }

   private void buscaProdutosAPI(DadosCarregadosCallback<List<Produto>> callback) {
      Call<List<Produto>> call = service.buscaTodos();
      call.enqueue(new CallbackComRetorno<>(new CallbackComRetorno.RespostaCallback<List<Produto>>() {
         @Override
         public void quandoSucesso(List<Produto> resultado) {
            atualizaInterno(resultado, callback);
         }

         @Override
         public void quandoFalha(String erro) {
            callback.quandoFalha(erro);
         }
      }));
   }

   public void edita(Produto produto, DadosCarregadosCallback<Produto> callback) {
      editaNaApi(produto, callback);
   }

   public void remove(Produto produto, DadosCarregadosCallback<Void> callback) {
      removeNaApi(produto, callback);
   }

   private void removeNaApi(Produto produto, DadosCarregadosCallback<Void> callback) {
      Call<Void> call = service.remove(produto.getId());
      call.enqueue(new CallbackSemRetorno(new CallbackSemRetorno.RespostaCallback() {
         @Override
         public void quandoSucesso() {
            removeInterno(produto, callback);
         }

         @Override
         public void quandoFalha(String erro) {
            callback.quandoFalha(erro);
         }
      }));
   }

   private void removeInterno(Produto produto, DadosCarregadosCallback<Void> callback) {
      new BaseAsyncTask<>(() -> {
         dao.remove(produto);
         return null;
      }, callback::quandoSucesso).execute();
   }

   private void editaNaApi(Produto produto, DadosCarregadosCallback<Produto> callback) {
      Call<Produto> call = service.edita(produto.getId(), produto);
      call.enqueue(new CallbackComRetorno<>(new CallbackComRetorno.RespostaCallback<Produto>() {
         @Override
         public void quandoSucesso(Produto resultado) {
            editaInterno(resultado, callback);
         }

         @Override
         public void quandoFalha(String erro) {
            callback.quandoFalha(erro);
         }
      }));
   }

   private void editaInterno(Produto produto, DadosCarregadosCallback<Produto> callback) {
      new BaseAsyncTask<>(() -> {
         dao.atualiza(produto);
         return produto;
      }, callback::quandoSucesso).execute();
   }

   private void atualizaInterno(List<Produto> produtos, DadosCarregadosCallback<List<Produto>> callback) {
      new BaseAsyncTask<>(() -> {
         dao.salva(produtos);
         return dao.buscaTodos();
      }, callback::quandoSucesso).execute();
   }

   public interface DadosCarregadosCallback<T> {
      void quandoSucesso(T resultado);

      void quandoFalha(String erro);
   }
}