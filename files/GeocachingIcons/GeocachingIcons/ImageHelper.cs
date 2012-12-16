using System;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using ImageTools;
using ImageTools.IO.Jpeg;
using ImageTools.IO.Png;

namespace GeocachingIcons
{
    public static class ImageHelper
    {
        public static void SaveImage(this UIElement uiElement)
        {
            var dialog = new SaveFileDialog
            {
                DefaultExt = ".png",
                Filter = "PNG | *.png | JPG | *.jpg",
            };
            var save = dialog.ShowDialog();
            if (save.HasValue && save.Value)
            {
                var saveStream = dialog.OpenFile();

                var bitmap = new WriteableBitmap(uiElement, new TranslateTransform());
                var image = bitmap.ToImage();
                if (dialog.SafeFileName.EndsWith(".png"))
                {
                    var encoder = new PngEncoder();
                    encoder.Encode(image, saveStream);
                }
                else if (dialog.SafeFileName.EndsWith(".jpg"))
                {
                    var encoder = new JpegEncoder();
                    encoder.Encode(image, saveStream);
                }

                saveStream.Close();
            }
        }

        public static void LoadImage(this BitmapImage bitmapImage)
        {
            var dialog = new OpenFileDialog
            {
                Filter = "Картинки (png, jpeg or gif)|*.png;*.jpg;*.gif;*.jpeg|Все файлы|*.*"
            };

            dialog.ShowDialog();
            if (dialog.File != null)
            {
                using (var fileStream = dialog.File.OpenRead())
                {
                    try
                    {
                        bitmapImage.SetSource(fileStream);
                    }
                    catch (Exception ex)
                    {
                        System.Diagnostics.Debug.WriteLine(ex.ToString());
                    }
                }
            }
        }

    }
}
