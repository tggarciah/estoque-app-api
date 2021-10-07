package br.com.alura.estoque.retrofit;

import androidx.annotation.NonNull;

import br.com.alura.estoque.retrofit.service.ProdutoService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EstoqueRetrofit {

   private static final String URL_BASE = "http://10.3.202.197:8080/";
   private final ProdutoService produtoService;

   public EstoqueRetrofit() {
      OkHttpClient client = configuraClienteInterceptor();

      Retrofit retrofit = new Retrofit.Builder()
              .baseUrl(URL_BASE)
              .addConverterFactory(GsonConverterFactory.create())
              .client(client)
              .build();

      produtoService = retrofit.create(ProdutoService.class);
   }

   @NonNull
   private OkHttpClient configuraClienteInterceptor() {
      HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
      logging.setLevel(HttpLoggingInterceptor.Level.BODY);
      OkHttpClient client = new OkHttpClient.Builder()
              .addInterceptor(logging)
              .build();
      return client;
   }

   public ProdutoService getProdutoService() {
      return produtoService;
   }
}
