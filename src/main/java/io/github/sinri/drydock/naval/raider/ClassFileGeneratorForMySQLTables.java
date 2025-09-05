package io.github.sinri.drydock.naval.raider;

import io.github.sinri.keel.integration.mysql.KeelMySQLDataSourceProvider;
import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.NamedMySQLDataSource;
import io.github.sinri.keel.integration.mysql.dev.TableRowClassSourceCodeGenerator;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * Abstract base class for generating Java class files from MySQL database tables.
 * <p>
 * This class provides functionality to automatically generate Java POJO classes
 * that correspond to MySQL table structures. It supports schema-based organization
 * and customizable package structures for the generated classes.
 * <p>
 * The generator creates classes in a hierarchical package structure:
 * {@code <tablePackage>.<dataSourceName>.<schemaPackageName>}
 * <p>
 * Features include:
 * <ul>
 *   <li>Automatic directory creation for generated classes</li>
 *   <li>Backup and restore of existing class files during regeneration</li>
 *   <li>Support for strict enum and envelope package configurations</li>
 *   <li>Configurable constant generation for schema and table names</li>
 * </ul>
 *
 * @since 1.2.4
 */
abstract public class ClassFileGeneratorForMySQLTables extends Privateer {

    /**
     * Gets the absolute path to the root package directory for generated table classes.
     * <p>
     * The path is read from the configuration property {@code table.package.path}.
     * This directory serves as the base location where all generated class files
     * will be organized by data source and schema.
     *
     * @return the absolute path to the table package root directory (without trailing slash)
     * @throws RuntimeException if the configuration property is not set or blank
     */
    protected String getTablePackagePath() {
        var p = Keel.config("table.package.path");
        if (p == null || p.isBlank()) {
            throw new RuntimeException("The table package path not set in config as `table.package.path`!");
        }
        return p;
    }

    /**
     * Gets the base package namespace for generated table classes.
     * <p>
     * This method should return the root package name where all generated
     * table classes will be placed. The returned string should not end with a dot.
     *
     * @return the base package namespace for table classes (without trailing dot)
     */
    abstract protected String getTablePackage();

    /**
     * Gets the package name for strict enum classes.
     * <p>
     * If provided, generated enum classes will be placed in this package.
     * This allows for better organization of enum types used in table classes.
     *
     * @return the package name for strict enum classes, or null if not specified
     */
    @Nullable
    abstract public String getStrictEnumPackage();

    /**
     * Gets the package name for envelope classes.
     * <p>
     * If provided, generated envelope classes will be placed in this package.
     * Envelope classes are typically used for wrapping table data with additional metadata.
     *
     * @return the package name for envelope classes, or null if not specified
     */
    @Nullable
    abstract public String getEnvelopePackage();

    /**
     * Determines whether to generate schema name constants.
     * <p>
     * When enabled, the generator will create constant fields containing
     * the schema name for easy reference in generated classes.
     *
     * @return true if schema constants should be generated, false otherwise
     */
    public boolean isProvideConstSchema() {
        return false;
    }

    /**
     * Determines whether to generate table name constants.
     * <p>
     * When enabled, the generator will create constant fields containing
     * the table name for easy reference in generated classes.
     *
     * @return true if table constants should be generated, false otherwise
     */
    public boolean isProvideConstTable() {
        return false;
    }

    /**
     * Determines whether to generate combined schema and table name constants.
     * <p>
     * When enabled, the generator will create constant fields containing
     * the full qualified table name (schema.table) for easy reference.
     *
     * @return true if combined schema and table constants should be generated, false otherwise
     */
    public boolean isProvideConstSchemaAndTable() {
        return true;
    }

    /**
     * Builds a valid Java package name from a MySQL schema name.
     * <p>
     * Converts the schema name to a valid Java package identifier by:
     * <ul>
     *   <li>Removing all non-alphanumeric characters</li>
     *   <li>Converting to lowercase</li>
     * </ul>
     *
     * @param schemaName the MySQL schema name to convert
     * @return a valid Java package name derived from the schema name
     * @throws IllegalArgumentException if the resulting package name is empty
     */
    @Nonnull
    protected String buildPackageNameForSchema(@Nonnull String schemaName) {
        var x = schemaName.replaceAll("[^A-Za-z0-9]+", "").toLowerCase();
        if (x.isBlank()) throw new IllegalArgumentException("SCHEMA PACKAGE NAME EMPTY");
        return x;
    }

    /**
     * Rebuilds Java class files for all tables in a MySQL schema.
     * <p>
     * This method generates Java POJO classes for all tables in the specified schema.
     * The generated classes will be organized in a package structure based on the
     * data source name and schema name.
     *
     * @param <C> the type of named MySQL connection
     * @param dataSourceName the name of the MySQL data source
     * @param sqlConnectionWrapper function to wrap SQL connections
     * @param schemaName the name of the MySQL schema to process
     * @return a Future that completes when all table classes are generated
     */
    protected <C extends NamedMySQLConnection> Future<Void> rebuildTablesInSchema(
            String dataSourceName,
            Function<SqlConnection, C> sqlConnectionWrapper,
            String schemaName
    ) {
        return rebuildTablesInSchema(dataSourceName, sqlConnectionWrapper, schemaName,
                buildPackageNameForSchema(schemaName), null);
    }

    /**
     * Rebuilds Java class files for specific tables in a MySQL schema.
     * <p>
     * This method generates Java POJO classes for only the specified tables
     * in the given schema. If the tables list is null or empty, all tables
     * in the schema will be processed.
     *
     * @param <C> the type of named MySQL connection
     * @param dataSourceName the name of the MySQL data source
     * @param sqlConnectionWrapper function to wrap SQL connections
     * @param schemaName the name of the MySQL schema to process
     * @param tables optional list of specific table names to process (null for all tables)
     * @return a Future that completes when all specified table classes are generated
     */
    protected <C extends NamedMySQLConnection> Future<Void> rebuildTablesInSchema(
            String dataSourceName,
            Function<SqlConnection, C> sqlConnectionWrapper,
            String schemaName,
            @Nullable List<String> tables
    ) {
        return rebuildTablesInSchema(dataSourceName, sqlConnectionWrapper, schemaName,
                buildPackageNameForSchema(schemaName), tables);
    }

    private <C extends NamedMySQLConnection> Future<Void> rebuildTablesInSchema(
            String dataSourceName,
            Function<SqlConnection, C> sqlConnectionWrapper,
            String schemaName,
            String schemaPackageName,
            @Nullable List<String> tables
    ) {
        NamedMySQLDataSource<C> mySQLDataSource = KeelMySQLDataSourceProvider.initializeNamedMySQLDataSource(
                dataSourceName,
                sqlConnectionWrapper
        );

        var dir = getTablePackagePath() + "/" + dataSourceName + "/" + schemaPackageName;
        return Keel.getVertx().fileSystem().exists(dir)
                   .compose(existed -> {
                       if (!existed) {
                           return Keel.getVertx().fileSystem().mkdirs(dir);
                       } else {
                           return Future.succeededFuture();
                       }
                   })
                   .compose(dirEnsured -> {
                       getUnitLogger().debug("Table Row Class Directory Ensured as " + dir);
                       return this.stashOldClassFiles(dir)
                                  .compose((Void v) -> mySQLDataSource.withConnection(sqlConnection -> {
                                      var x = new TableRowClassSourceCodeGenerator(sqlConnection)
                                              .forSchema(schemaName);
                                      x.setLogger(getUnitLogger());
                                      if (tables != null) {
                                          x.forTables(tables);
                                      }
                                      x.setStandardHandler(standard -> {
                                          String strictEnumPackage = getStrictEnumPackage();
                                          if (strictEnumPackage != null) {
                                              standard.setStrictEnumPackage(strictEnumPackage);
                                          }
                                          String envelopePackage = getEnvelopePackage();
                                          if (envelopePackage != null) {
                                              standard.setEnvelopePackage(envelopePackage);
                                          }
                                          standard
                                                  .setProvideConstSchema(isProvideConstSchema())
                                                  .setProvideConstTable(isProvideConstTable())
                                                  .setProvideConstSchemaAndTable(isProvideConstSchemaAndTable());
                                          standard.setVcsFriendly(true);
                                      });

                                      return x.generate(
                                              getTablePackage() + "." + dataSourceName + "." + schemaPackageName,
                                              getTablePackagePath() + "/" + dataSourceName + "/" + schemaPackageName
                                      );
                                  }))
                                  .compose(
                                          v -> this.removeOldClassFiles(dir),
                                          failure -> this.callbackOldClassFiles(dir)
                                                         .eventually(() -> {
                                                             return Future.failedFuture(failure);
                                                         })
                                  );
                   });
    }

    /**
     * Stashes existing class files by renaming them with .stash extension.
     * <p>
     * This method backs up existing generated class files before regeneration
     * to allow for rollback in case of generation failures. Package-info.java
     * files are preserved without modification.
     *
     * @param dir the directory containing class files to stash
     * @return a Future that completes when all files are stashed
     * @since 1.5.11
     */
    private Future<Void> stashOldClassFiles(String dir) {
        getUnitLogger().notice("stashOldClassFiles");
        return Keel.getVertx().fileSystem().readDir(dir)
                   .compose(files -> {
                       return Keel.asyncCallIteratively(files, file -> {
                           if (file.endsWith("/package-info.java")) {
                               return Future.succeededFuture();
                           } else {
                               //return Keel.getVertx().fileSystem().delete(file);
                               return Keel.getVertx().fileSystem().move(file, file + ".stash");
                           }
                       });
                   });
    }

    /**
     * Restores stashed class files by removing the .stash extension.
     * <p>
     * This method is called when class generation fails to restore the
     * previously backed up files, ensuring no data loss during failed operations.
     *
     * @param dir the directory containing stashed files to restore
     * @return a Future that completes when all files are restored
     * @since 1.5.11
     */
    private Future<Void> callbackOldClassFiles(String dir) {
        getUnitLogger().warning("callbackOldClassFiles");
        return Keel.getVertx().fileSystem().readDir(dir)
                   .compose(files -> {
                       return Keel.asyncCallIteratively(files, file -> {
                           if (file.endsWith(".stash")) {
                               String x = file.substring(0, file.length() - ".stash".length());
                               return Keel.getVertx().fileSystem().move(file, x);
                           }
                           return Future.succeededFuture();
                       });
                   });
    }

    /**
     * Permanently removes stashed class files after successful generation.
     * <p>
     * This method cleans up the backup files created during the stashing process
     * after new class files have been successfully generated.
     *
     * @param dir the directory containing stashed files to remove
     * @return a Future that completes when all stashed files are removed
     * @since 1.5.11
     */
    private Future<Void> removeOldClassFiles(String dir) {
        getUnitLogger().notice("removeOldClassFiles");
        return Keel.getVertx().fileSystem().readDir(dir)
                   .compose(files -> {
                       return Keel.asyncCallIteratively(files, file -> {
                           if (file.endsWith(".stash")) {
                               return Keel.getVertx().fileSystem().delete(file);
                           }
                           return Future.succeededFuture();
                       });
                   });
    }
}
