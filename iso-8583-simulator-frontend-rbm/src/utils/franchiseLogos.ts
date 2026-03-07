export function getFranchiseLogo(fileName?: string | null): string {
  if (!fileName) return '/franchises/logo_unknown.svg';

  const safeFileName = fileName.toLowerCase().replace(/[^a-z0-9]/g, '');
  const path = `/franchises/logo_${safeFileName}.svg`;

  // Retorna la ruta, pero si el archivo no existe se reemplaza en onError
  return path;
}
